package com.tygron.tools.explorer.map.parsers;

import java.io.File;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javax.imageio.ImageIO;
import com.tygron.pub.logger.Log;
import com.tygron.pub.utils.StringUtils;
import com.tygron.tools.explorer.gui.MapPaneSubPane;

public class ImageSaveMapModule extends AbstractMapModule {

	public class NumberTextField extends TextField {
		@Override
		public void replaceSelection(String text) {
			if (validate(text)) {
				super.replaceSelection(text);
			}
		}

		@Override
		public void replaceText(int start, int end, String text) {
			if (validate(text)) {
				super.replaceText(start, end, text);
			}
		}

		private boolean validate(String text) {
			return ("".equals(text) || text.matches("[0-9]"));
		}
	}

	private enum SizeType {
			RENDER,
			MAP,
			CUSTOM;
	}

	private static final String FORMAT = "png";
	private MapPaneSubPane pane = null;

	private TextField imageSizeField = new NumberTextField();
	private SizeType imageSizeType = SizeType.RENDER;

	public ImageSaveMapModule() {
	}

	@Override
	public String getName() {
		return "Export map";
	}

	@Override
	public MapPaneSubPane getPane() {
		if (pane != null) {
			return pane;
		}

		pane = new MapPaneSubPane();

		VBox verticalBox = new VBox();
		verticalBox.setFillWidth(true);
		pane.fill(verticalBox, 0);

		HBox fileDetails = new HBox();
		fileDetails.setPadding(new Insets(10, 10, 0, 10));
		AnchorPane.setLeftAnchor(fileDetails, 0.0);
		AnchorPane.setRightAnchor(fileDetails, 0.0);

		Text text = new Text("File name: ");
		HBox.setHgrow(text, Priority.NEVER);
		TextField fileNameField = new TextField();
		HBox.setHgrow(fileNameField, Priority.ALWAYS);
		Button button = new Button("Save file");
		button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				new Thread() {
					@Override
					public void run() {
						saveMapHandle(imageSizeField, fileNameField);
					}
				}.start();

			}
		});
		HBox.setHgrow(button, Priority.NEVER);

		fileDetails.getChildren().addAll(text, fileNameField, button);

		GridPane settingsPane = new GridPane();

		settingsPane.setAlignment(Pos.CENTER);
		settingsPane.setHgap(10);
		settingsPane.setVgap(10);
		settingsPane.setPadding(new Insets(0, 10, 10, 10));

		Text imageSizeText = new Text("Output size");

		settingsPane.add(imageSizeText, 1, 1, 2, 1);

		ToggleGroup imageSizeGroup = new ToggleGroup();
		ToggleGroup renderSizeGroup = new ToggleGroup();

		// imageSizeField.setPrefWidth(imageSizeField.getPrefWidth() / 2);

		RadioButton imageSize1 = new RadioButton();
		RadioButton imageSize2 = new RadioButton();
		RadioButton imageSize3 = new RadioButton();
		Text sizeText1 = new Text("Render");
		Text sizeText2 = new Text("Map size");
		imageSize1.setToggleGroup(imageSizeGroup);
		imageSize2.setToggleGroup(imageSizeGroup);
		imageSize3.setToggleGroup(imageSizeGroup);
		imageSize1.setUserData(SizeType.RENDER);
		imageSize2.setUserData(SizeType.MAP);
		imageSize3.setUserData(SizeType.CUSTOM);
		imageSize1.setSelected(true);
		settingsPane.add(imageSize1, 1, 2, 1, 1);
		settingsPane.add(imageSize2, 1, 3, 1, 1);
		settingsPane.add(imageSize3, 1, 4, 1, 1);
		settingsPane.add(sizeText1, 2, 2, 1, 1);
		settingsPane.add(sizeText2, 2, 3, 1, 1);
		settingsPane.add(imageSizeField, 2, 4, 1, 1);

		ChangeListener<Toggle> changeListener = new ChangeListener<Toggle>() {
			@Override
			public void changed(ObservableValue<? extends Toggle> ov, Toggle oldToggle, Toggle newToggle) {
				if (newToggle == null) {
					return;
				}

				SizeType selectedSize = (SizeType) newToggle.getUserData();

				if (newToggle.getToggleGroup().equals(imageSizeGroup)) {
					imageSizeType = selectedSize;
				}
			}
		};

		imageSizeGroup.selectedToggleProperty().addListener(changeListener);
		renderSizeGroup.selectedToggleProperty().addListener(changeListener);

		verticalBox.getChildren().addAll(fileDetails, settingsPane);
		pane.getChildren().add(verticalBox);
		return pane;
	}

	public void saveMap(int size, String fileName) {
		Image image = null;
		if (size <= 0) {
			setStatus("Size of the image must be greater than 0.");
			return;
		}
		if (StringUtils.isEmpty(fileName)) {
			setStatus("A filename must be provided.");
			return;
		}
		String completeFileName = fileName;
		if (completeFileName.indexOf('.') < 0) {
			completeFileName += "." + FORMAT;
		}

		image = getRenderManager().getRenderedImage(size);

		if (image == null) {
			Log.warning("Failed to render an image.");
			return;
		}

		File file = new File(completeFileName);

		try {
			ImageIO.write(SwingFXUtils.fromFXImage(image, null), FORMAT, file);
		} catch (Exception e) {
			Log.exception(e, "Exception occurred while writing image.");
		}
	}

	public void saveMapHandle(final TextField sizeField, final TextField fileNameField) {
		int imageSize = (int) getRenderManager().getRenderSize();
		switch (imageSizeType) {
			case RENDER:
				imageSize = (int) getRenderManager().getRenderSize();
				break;
			case MAP:
				imageSize = getRenderManager().getMapSize();
				break;
			case CUSTOM:
				try {
					imageSize = Integer.parseInt(sizeField.getText());
				} catch (Exception e) {
					Log.exception(e, "Failed to parse custom value for image size.");
					setStatus("Failed to parse custom value for image size.");
					return;
				}
				break;
		}

		final int finalImageSize = imageSize;

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				saveMap(finalImageSize, fileNameField.getText());
			}
		});
	}

	private void zzzPrintMap() {
		/*
		 * int size = renderManager.getMapSize(); if (size <= 0) { size = DEFAULT_RENDER_PANE_SIZE;
		 * Log.info("Size for printing assumed to " + size); } Image image =
		 * renderManager.getRenderedImage(size); ImageView imageView = new ImageView(image); int targetSize =
		 * PAGE_FIT_SIZE; imageView.setFitWidth(targetSize); imageView.setFitHeight(targetSize); PrinterJob
		 * job = PrinterJob.createPrinterJob(); boolean showDialog = job.showPageSetupDialog(new
		 * Stage(StageStyle.DECORATED)); if (showDialog) { if (job != null) { boolean success =
		 * job.printPage(imageView); if (success) { job.endJob(); } } }
		 */
	}
}
