package com.tygron.tools.explorer.gui;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
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
import javafx.scene.text.Text;
import javax.imageio.ImageIO;
import com.tygron.pub.api.data.misc.LocationObject;
import com.tygron.pub.api.enums.MapLink;
import com.tygron.pub.logger.Log;
import com.tygron.pub.utils.StringUtils;
import com.tygron.pub.utils.ValueUtils;
import com.tygron.tools.explorer.logic.ExplorerCommunicator;
import com.tygron.tools.explorer.map.MapRenderManager;
import com.tygron.tools.explorer.map.MapRenderManager.RenderManagerListener;
import com.tygron.tools.explorer.map.parsers.AbstractMapModule;
import com.tygron.tools.explorer.map.parsers.ImageSaveMapModule;
import com.tygron.tools.explorer.map.parsers.PipeMapModule;
import com.tygron.tools.explorer.map.parsers.PrintFunctionsModule;
import com.tygron.tools.explorer.map.parsers.RemoteMeasureModule;

public class MapPane extends GameExplorerSubPane implements RenderManagerListener {

	private static final int DEFAULT_RENDER_PANE_SIZE = 400;

	private static final int PAGE_FIT_SIZE = 486;

	private final MapRenderManager renderManager;

	private Text mapLoadingText = new Text("Map is loading, please wait");
	private Text mapFailedText = new Text("Map has failed to load");

	private VBox verticalPane = new VBox();
	private Pane mapPaneAndRenderContainer = new Pane();
	private StackPane mapRenderPane = new StackPane();
	private StackPane mapContainer = new StackPane();
	private Pane map = new Pane();

	private ComboBox<AbstractMapModule> modeSelectionBox = new ComboBox<AbstractMapModule>();

	private VBox mapFunctionAndSwitcherContainer = new VBox();
	private ScrollPane mapFunctionSubPaneContainer = new ScrollPane();

	public MapPane(ExplorerCommunicator communicator) {
		super(communicator);

		// mapRenderPane.setStyle("-fx-background-color: rgba(128, 128, 128, 1.0);");

		// mapPaneAndRenderContainer.setStyle("-fx-background-color: rgba(128, 0, 0, 1.0);");
		// mapContainer.setStyle("-fx-background-color: rgba(0, 128, 0, 1.0);");

		mapFailedText.setVisible(false);
		mapFailedText.setFill(Color.CRIMSON);
		mapFailedText.setStyle("-fx-font-size:20; -fx-font-weight:bold;");
		mapLoadingText.setStyle("-fx-font-size:20; -fx-font-weight:bold;");
		mapContainer.getChildren().add(mapFailedText);

		verticalPane.setFillWidth(true);
		GameExplorerPane.fill(verticalPane, 0.0);

		mapPaneAndRenderContainer.setMinHeight(0.0);
		mapPaneAndRenderContainer.prefHeightProperty().bind(
				verticalPane.heightProperty().subtract(mapFunctionAndSwitcherContainer.heightProperty()));
		mapPaneAndRenderContainer.maxHeightProperty().bind(
				verticalPane.heightProperty().subtract(mapFunctionAndSwitcherContainer.heightProperty()));

		mapPaneAndRenderContainer.minWidthProperty().bind(verticalPane.widthProperty());
		mapPaneAndRenderContainer.maxWidthProperty().bind(verticalPane.widthProperty());

		map.setMinWidth(0.0);
		map.prefWidthProperty().bind(mapContainer.heightProperty());
		map.maxWidthProperty().bind(mapContainer.heightProperty());
		map.setMinHeight(0.0);
		map.prefHeightProperty().bind(mapContainer.widthProperty());
		map.maxHeightProperty().bind(mapContainer.widthProperty());

		map.setVisible(false);

		mapContainer.minWidthProperty().bind(verticalPane.widthProperty());
		mapContainer.prefWidthProperty().bind(verticalPane.widthProperty());
		mapContainer.maxWidthProperty().bind(verticalPane.widthProperty());

		mapContainer.prefHeightProperty().bind(
				verticalPane.heightProperty().subtract(mapFunctionAndSwitcherContainer.heightProperty()));
		mapContainer.maxHeightProperty().bind(
				verticalPane.heightProperty().subtract(mapFunctionAndSwitcherContainer.heightProperty()));

		mapContainer.getChildren().add(map);
		mapPaneAndRenderContainer.getChildren().addAll(mapRenderPane, mapContainer);
		mapContainer.toFront();

		verticalPane.getChildren().add(mapPaneAndRenderContainer);

		this.getChildren().add(verticalPane);

		mapFunctionAndSwitcherContainer.setMinHeight(400.0);
		mapFunctionAndSwitcherContainer.setPrefHeight(400.0);
		mapFunctionAndSwitcherContainer.setFillWidth(true);

		HBox mapFunctionSelectionPane = new HBox();
		VBox.setVgrow(mapFunctionSelectionPane, Priority.NEVER);
		mapFunctionSelectionPane.setPadding(new Insets(10, 10, 10, 10));
		mapFunctionSelectionPane.minWidthProperty().bind(verticalPane.widthProperty());
		mapFunctionSelectionPane.maxWidthProperty().bind(verticalPane.widthProperty());
		Text functionSelectionText = new Text("Mode: ");
		HBox.setHgrow(functionSelectionText, Priority.NEVER);

		HBox.setHgrow(modeSelectionBox, Priority.ALWAYS);
		mapFunctionSelectionPane.getChildren().addAll(functionSelectionText, modeSelectionBox);

		// mapFunctionSubPaneContainer.setStyle("-fx-background-color: rgba(0, 128, 0, 1.0);");

		VBox.setVgrow(mapFunctionSubPaneContainer, Priority.ALWAYS);
		mapFunctionSubPaneContainer.setFitToHeight(true);
		mapFunctionSubPaneContainer.setFitToWidth(true);

		mapFunctionAndSwitcherContainer.getChildren().addAll(mapFunctionSelectionPane,
				mapFunctionSubPaneContainer);

		// modeSelectionBox.getItems().addAll(new ImageSaveMapModule(), new PipeMapModule());

		modeSelectionBox.valueProperty().addListener(new ChangeListener<AbstractMapModule>() {
			@Override
			public void changed(ObservableValue<? extends AbstractMapModule> ov, AbstractMapModule oldValue,
					AbstractMapModule newValue) {
				if (oldValue != null) {
					oldValue.unload();
				}

				mapFunctionSubPaneContainer.setContent(newValue.getPane());

				if (newValue != null) {
					newValue.load();
				}
			}
		});

		verticalPane.getChildren().add(mapFunctionAndSwitcherContainer);

		renderManager = new MapRenderManager(mapRenderPane);
		renderManager.addRenderManagerListener(this);

		for (AbstractMapModule module : modeSelectionBox.getItems()) {
			module.setCommunicator(getCommunicator());
			module.setRenderManager(this.renderManager);
		}

		// mapFunctionSubPaneContainer.setContent(new ImageSaveMapModule());

		//
		//
		//
		//
		//

		if (true) {
			return;
		}

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
						Log.info("Map width: " + map.getWidth());
						Log.info("MapContainer width: " + mapContainer.getWidth());
						Log.info("MapPaneAndRenderContainer width: " + mapPaneAndRenderContainer.getWidth());
						renderManager.displayUserDefinedPolygon(polygonField.getText());
					}
				});
			}
		});
		polygonHBox.setPadding(new Insets(25, 25, 25, 25));

		polygonHBox.prefWidthProperty().bind(polygonPane.widthProperty());
		polygonHBox.getChildren().addAll(polygonText, polygonField, polygonButton);
		polygonPane.getChildren().add(polygonHBox);
		verticalPane.getChildren().add(polygonPane);

		Button printButton = new Button("Print");
		printButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						// printMap();
						saveMap();
					}
				});
			}
		});

		verticalPane.getChildren().add(printButton);
	}

	private void displayRenderedImage() {
		displayRenderedImage(null);
	}

	private void displayRenderedImage(final Image image) {

		Platform.runLater(new Runnable() {
			@Override
			public void run() {

				Image imageToUse = image;
				long renderStartTime = System.currentTimeMillis();
				if (imageToUse == null) {
					imageToUse = renderManager.getRenderedImage();
				}

				BackgroundImage backgroundImage = new BackgroundImage(imageToUse, BackgroundRepeat.NO_REPEAT,
						BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, new BackgroundSize(20, 20,
								false, false, true, false));

				map.setBackground(new Background(backgroundImage));
				map.setVisible(true);

				long renderTime = System.currentTimeMillis() - renderStartTime;
				Log.info("Time between render and display: " + renderTime);
			}
		});
	}

	private String generateImageURL() {
		LocationObject location = getCommunicator().getLocation();
		if (location.isEmpty()) {
			return null;
		}

		String baseURL = "http://server.arcgisonline.com/arcgis/rest/services/World_Topo_Map/MapServer/export?bbox=%S%%2C+%S%%2C+%S%%2C+%S&bboxSR=%S&imageSR=&format=png&transparent=false&f=image";
		String url = String.format(baseURL, location.getMinX(), location.getMinY(), location.getMaxX(),
				location.getMaxY(), location.getFormatNumber());
		Log.info("Map: " + url);
		return url;
	}

	public void loadMap() {
		String url = generateImageURL();
		if (StringUtils.isEmpty(url)) {
			return;
		}

		mapContainer.getChildren().add(mapLoadingText);

		Image mapImage = new Image(url, true);

		mapFailedText.visibleProperty().bind(mapImage.errorProperty());
		mapLoadingText.visibleProperty().bind(
				mapImage.progressProperty().isEqualTo(1).or(mapImage.errorProperty()).not());

		BackgroundImage backgroundImage = new BackgroundImage(mapImage, BackgroundRepeat.NO_REPEAT,
				BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, new BackgroundSize(20, 20, false,
						false, true, false));

		mapRenderPane.setBackground(new Background(backgroundImage));
		displayRenderedImage(mapImage);
	}

	private List<AbstractMapModule> loadModules() {
		final List<AbstractMapModule> modules = new LinkedList<AbstractMapModule>();
		final List<AbstractMapModule> returnable = new LinkedList<AbstractMapModule>();
		modules.add(new ImageSaveMapModule());
		modules.add(new PipeMapModule());
		modules.add(new RemoteMeasureModule());
		modules.add(new PrintFunctionsModule());

		for (AbstractMapModule module : modules) {
			module.setCommunicator(getCommunicator());
			module.setRenderManager(this.renderManager);
			if (module.isFunctional()) {
				returnable.add(module);
			}
		}

		return returnable;
	}

	@Override
	public void processRenderUpdate() {
		displayRenderedImage();
	}

	@Override
	public void processUpdate() {
		retrieveMapSize();

		final List<AbstractMapModule> modules = loadModules();

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				modeSelectionBox.getItems().addAll(modules);
			}
		});
	}

	private void retrieveMapSize() {
		try {
			Map<String, ?> mapSizeSetting = (Map<String, ?>) getCommunicator().getData(MapLink.SETTINGS).get(
					ValueUtils.SETTING_MAP_SIZE);
			renderManager.setMapSize(Integer.parseInt((String) mapSizeSetting.get("value")));
		} catch (Exception e) {
			Log.exception(e, "Failed to load map size.");
			setStatus("Failed to load map size.");

		}
	}

	private void saveMap() {
		int size = renderManager.getMapSize();
		if (size <= 0) {
			size = DEFAULT_RENDER_PANE_SIZE;
			Log.info("Size for saving assumed to " + size);
		}
		Image image = renderManager.getRenderedImage(size);
		File file = new File("TestImage.png");

		try {
			ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
		} catch (Exception e) {
			Log.exception(e, "Exception occurred while writing image.");
		}
	}

	public void updateDisplayedData(final String mapLink, final Object data) {
		renderManager.displayData(mapLink, data);
	}
}
