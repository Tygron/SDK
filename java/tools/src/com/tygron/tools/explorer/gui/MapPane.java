package com.tygron.tools.explorer.gui;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import com.tygron.pub.api.data.misc.LocationObject;
import com.tygron.pub.api.enums.MapLink;
import com.tygron.pub.logger.Log;
import com.tygron.pub.utils.StringUtils;
import com.tygron.pub.utils.ValueUtils;
import com.tygron.tools.explorer.gui.map.ShapeUtils;
import com.tygron.tools.explorer.logic.ExplorerCommunicator;

public class MapPane extends GameExplorerSubPane {

	private static final String POLYGONS = "polygons";

	private Image mapImage = null;
	private Text mapLoadingText = new Text("Map is loading, please wait");
	private Text mapFailedText = new Text("Map has failed to load");

	private VBox verticalPane = new VBox();
	private StackPane mapContainer = new StackPane();
	private Pane map = new Pane();
	private Pane polygonGroup = new Pane();
	private Polygon mapSizePolygon = new Polygon();

	private Shape renderedPolygon = null;

	private volatile String currentHashCode = StringUtils.EMPTY;
	private SimpleIntegerProperty mapSize = new SimpleIntegerProperty();

	private HashMap<String, Collection<Shape>> shapeCache = new HashMap<String, Collection<Shape>>();

	public MapPane(ExplorerCommunicator communicator) {
		super(communicator);

		mapFailedText.setVisible(false);
		mapFailedText.setFill(Color.CRIMSON);
		mapFailedText.setStyle("-fx-font-size:20; -fx-font-weight:bold;");
		mapLoadingText.setStyle("-fx-font-size:20; -fx-font-weight:bold;");
		mapContainer.getChildren().add(mapFailedText);

		verticalPane.setFillWidth(true);
		GameExplorerPane.fill(verticalPane, 0.0);

		mapContainer.maxWidthProperty().bind(verticalPane.widthProperty());
		mapContainer.prefWidthProperty().bind(verticalPane.widthProperty());
		mapContainer.maxHeightProperty().bind(verticalPane.widthProperty());
		mapContainer.prefHeightProperty().bind(verticalPane.widthProperty());
		mapContainer.minHeightProperty().bind(verticalPane.heightProperty().divide(2));

		map.prefWidthProperty().bind(map.heightProperty());
		map.prefHeightProperty().bind(map.widthProperty());
		map.maxWidthProperty().bind(mapContainer.heightProperty());
		map.maxHeightProperty().bind(mapContainer.widthProperty());

		polygonGroup.scaleXProperty().bind(map.heightProperty().divide(mapSize));
		polygonGroup.scaleYProperty().bind(map.heightProperty().divide(mapSize));
		polygonGroup.layoutXProperty().bind(map.heightProperty().subtract(mapSize).divide(2).subtract(1));
		polygonGroup.layoutYProperty().bind(polygonGroup.layoutXProperty());
		polygonGroup.setStyle("-fx-background-color: rgba(0, 0, 0, 0.0);");

		mapSizePolygon.getPoints().addAll(0.0, 0.0, new Double(mapSize.get()), new Double(mapSize.get()));
		mapSizePolygon.setStroke(Color.TRANSPARENT);
		mapSizePolygon.setFill(Color.TRANSPARENT);

		polygonGroup.getChildren().add(mapSizePolygon);
		map.getChildren().add(polygonGroup);
		mapContainer.getChildren().add(map);
		verticalPane.getChildren().add(mapContainer);

		Pane polygonPane = new Pane();
		polygonPane.setMinHeight(100.0);
		polygonPane.setPrefHeight(100.0);

		HBox polygonHBox = new HBox();
		Text polygonText = new Text("Polygon: ");
		TextField polygonField = new TextField();
		HBox.setHgrow(polygonField, Priority.ALWAYS);
		Button polygonButton = new Button("Render");
		polygonButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						renderPolygon(polygonField.getText());
					}
				});
			}
		});
		polygonHBox.setPadding(new Insets(25, 25, 25, 25));

		polygonHBox.prefWidthProperty().bind(polygonPane.widthProperty());
		polygonHBox.getChildren().addAll(polygonText, polygonField, polygonButton);
		polygonPane.getChildren().add(polygonHBox);
		verticalPane.getChildren().add(polygonPane);

		this.getChildren().add(verticalPane);
	}

	public void displayData(final Collection<Shape> shapes) {
		String hashCode = Integer.toString(shapes.hashCode());
		currentHashCode = hashCode;

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (!currentHashCode.equals(hashCode)) {
					return;
				}
				polygonGroup.getChildren().retainAll(mapSizePolygon);
				polygonGroup.getChildren().addAll(shapes);
				if (renderedPolygon != null) {
					polygonGroup.getChildren().add(renderedPolygon);
				}
			}
		});
	}

	public void displayData(final Object dataObject) {
		// shapeCache.clear();

		String hashCode = Integer.toString(dataObject.hashCode());
		currentHashCode = hashCode;

		if (!(dataObject instanceof Map<?, ?>)) {
			polygonGroup.getChildren().retainAll(mapSizePolygon, renderedPolygon);
			return;
		}
		polygonGroup.getChildren().retainAll(mapSizePolygon, renderedPolygon);

		Map<String, ? extends Object> data = (Map<String, ? extends Object>) dataObject;

		Thread shapeThread = new Thread() {
			@Override
			public void run() {
				final Collection<Shape> newShapes = new LinkedList<Shape>();

				if (data.containsKey(POLYGONS)) {
					Shape newShape = ShapeUtils.createPolygon((String) data.get(POLYGONS));
					if (newShape != null) {
						newShapes.add(newShape);
					}
				} else {

					boolean alertedStatus = false;

					if (!shapeCache.containsKey(hashCode)) {
						shapeCache.put(hashCode, newShapes);

						for (Object o : data.values()) {
							if (!(o instanceof Map<?, ?>)) {
								continue;
							}
							Map<?, ?> subMap = (Map<?, ?>) o;
							if (!subMap.containsKey(POLYGONS)) {
								continue;
							}
							if (!alertedStatus) {
								getCommunicator().setStatus("Rendering polygons");
								alertedStatus = true;
							}
							Shape polygon = ShapeUtils.createPolygon((String) subMap.get(POLYGONS));
							if (polygon == null) {
								continue;
							}
							newShapes.add(polygon);
						}
						if (alertedStatus) {
							getCommunicator().setStatus("Done rendering polygons");
						}
					} else {
						newShapes.addAll(shapeCache.get(hashCode));
					}
				}

				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						if (!currentHashCode.equals(hashCode)) {
							return;
						}
						polygonGroup.getChildren().retainAll(mapSizePolygon);
						polygonGroup.getChildren().addAll(newShapes);
						if (renderedPolygon != null) {
							polygonGroup.getChildren().add(renderedPolygon);
						}
					}
				});
			}
		};
		shapeThread.setDaemon(true);
		shapeThread.start();
	}

	private String generateImageURL() {
		String baseURL = "http://server.arcgisonline.com/arcgis/rest/services/World_Topo_Map/MapServer/export?bbox=%S%%2C+%S%%2C+%S%%2C+%S&bboxSR=%S&imageSR=&format=png&transparent=false&f=image";

		LocationObject location = getCommunicator().getLocation();

		String url = String.format(baseURL, location.getMinX(), location.getMinY(), location.getMaxX(),
				location.getMaxY(), location.getFormatNumber());

		Log.info("Map: " + url);
		return url;
	}

	public void loadMap() {
		mapContainer.getChildren().add(mapLoadingText);

		String url = generateImageURL();
		mapImage = new Image(url, true);

		mapFailedText.visibleProperty().bind(mapImage.errorProperty());
		mapLoadingText.visibleProperty().bind(
				mapImage.progressProperty().isEqualTo(1).or(mapImage.errorProperty()).not());

		BackgroundImage backgroundImage = new BackgroundImage(mapImage, BackgroundRepeat.NO_REPEAT,
				BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, new BackgroundSize(20, 20, false,
						false, true, false));

		map.setBackground(new Background(backgroundImage));
	}

	@Override
	public void processUpdate() {
		Map<String, ?> mapSizeSetting = (Map<String, ?>) getCommunicator().getData(MapLink.SETTINGS).get(
				ValueUtils.SETTING_MAP_SIZE);
		mapSize.set(Integer.parseInt((String) mapSizeSetting.get("value")));

		mapSizePolygon.getPoints().clear();
		mapSizePolygon.getPoints().addAll(0.0, 0.0, new Double(mapSize.get()), new Double(mapSize.get()));
		mapSizePolygon.setStrokeWidth(0);
	}

	private void renderPolygon(String polygonString) {
		if (renderedPolygon != null) {
			polygonGroup.getChildren().remove(renderedPolygon);
		}

		renderedPolygon = ShapeUtils.createPolygon(polygonString);
		if (renderedPolygon != null) {
			renderedPolygon.setFill(Color.CRIMSON);
			polygonGroup.getChildren().add(renderedPolygon);
		}
	}
}
