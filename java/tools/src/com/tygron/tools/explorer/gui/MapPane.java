package com.tygron.tools.explorer.gui;

import java.io.File;
import java.util.Map;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.print.PrinterJob;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.imageio.ImageIO;
import com.tygron.pub.api.data.misc.LocationObject;
import com.tygron.pub.api.enums.MapLink;
import com.tygron.pub.logger.Log;
import com.tygron.pub.utils.StringUtils;
import com.tygron.pub.utils.ValueUtils;
import com.tygron.tools.explorer.logic.ExplorerCommunicator;
import com.tygron.tools.explorer.map.MapRenderManager;
import com.tygron.tools.explorer.map.MapRenderManager.RenderManagerListener;

public class MapPane extends GameExplorerSubPane implements RenderManagerListener {

	private static final int DEFAULT_RENDER_PANE_SIZE = 400;

	private static final int PAGE_FIT_SIZE = 486;

	private final MapRenderManager renderManager;

	private Text mapLoadingText = new Text("Map is loading, please wait");
	private Text mapFailedText = new Text("Map has failed to load");

	private VBox verticalPane = new VBox();
	private Pane mapPaneAndRenderContainer = new Pane();
	private Pane mapRenderPane = new Pane();
	private StackPane mapContainer = new StackPane();
	private Pane map = new Pane();

	public MapPane(ExplorerCommunicator communicator) {
		super(communicator);

		mapFailedText.setVisible(false);
		mapFailedText.setFill(Color.CRIMSON);
		mapFailedText.setStyle("-fx-font-size:20; -fx-font-weight:bold;");
		mapLoadingText.setStyle("-fx-font-size:20; -fx-font-weight:bold;");
		mapContainer.getChildren().add(mapFailedText);

		verticalPane.setFillWidth(true);
		GameExplorerPane.fill(verticalPane, 0.0);

		mapPaneAndRenderContainer.setMinWidth(0.0);
		mapPaneAndRenderContainer.setMinHeight(0.0);
		mapPaneAndRenderContainer.prefWidthProperty().bind(verticalPane.widthProperty());
		mapPaneAndRenderContainer.maxWidthProperty().bind(verticalPane.widthProperty());

		mapRenderPane.setStyle("-fx-background-color: rgba(128, 128, 128, 1.0);");

		map.prefWidthProperty().bind(map.heightProperty());
		map.prefHeightProperty().bind(map.widthProperty());
		map.maxWidthProperty().bind(map.heightProperty());
		map.maxHeightProperty().bind(mapContainer.heightProperty());

		map.setVisible(false);

		mapContainer.minWidthProperty().bind(verticalPane.widthProperty());
		mapContainer.maxWidthProperty().bind(verticalPane.widthProperty());
		mapContainer.prefWidthProperty().bind(verticalPane.widthProperty());
		mapContainer.maxHeightProperty().bind(verticalPane.widthProperty());

		mapContainer.prefHeightProperty().bind(mapPaneAndRenderContainer.heightProperty());
		mapContainer.prefWidthProperty().bind(mapPaneAndRenderContainer.widthProperty());

		mapContainer.getChildren().add(map);
		mapPaneAndRenderContainer.getChildren().addAll(mapRenderPane, mapContainer);
		mapContainer.toFront();

		verticalPane.getChildren().add(mapPaneAndRenderContainer);

		this.getChildren().add(verticalPane);

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

		renderManager = new MapRenderManager(mapRenderPane);
		renderManager.addRenderManagerListener(this);
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

	@Override
	public void processRenderUpdate() {
		displayRenderedImage();
	}

	@Override
	public void processUpdate() {
		retrieveMapSize();
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
		Image image = renderManager.getRenderedImage(size, renderManager.getMapSize());
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

	private void zzzPrintMap() {
		int size = renderManager.getMapSize();
		if (size <= 0) {
			size = DEFAULT_RENDER_PANE_SIZE;
			Log.info("Size for printing assumed to " + size);
		}
		Image image = renderManager.getRenderedImage(size);

		ImageView imageView = new ImageView(image);

		int targetSize = PAGE_FIT_SIZE;

		imageView.setFitWidth(targetSize);
		imageView.setFitHeight(targetSize);

		PrinterJob job = PrinterJob.createPrinterJob();
		boolean showDialog = job.showPageSetupDialog(new Stage(StageStyle.DECORATED));
		if (showDialog) {

			if (job != null) {
				boolean success = job.printPage(imageView);
				if (success) {
					job.endJob();
				}
			}
		}
	}
}
