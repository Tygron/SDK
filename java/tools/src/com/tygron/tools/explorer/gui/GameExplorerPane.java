package com.tygron.tools.explorer.gui;

import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import com.tygron.tools.explorer.logic.ExplorerCommunicator;

public class GameExplorerPane extends AnchorPane {

	protected static void fill(Node node, double fit) {
		AnchorPane.setBottomAnchor(node, fit);
		AnchorPane.setTopAnchor(node, fit);
		AnchorPane.setLeftAnchor(node, fit);
		AnchorPane.setRightAnchor(node, fit);
	}

	private final ExplorerCommunicator communicator = new ExplorerCommunicator();

	private final GridPane root = new GridPane();
	private final VBox divideContainer = new VBox();

	private final HBox gamePaneContainer = new HBox();
	private final Pane dataPaneContainer = new Pane();

	private final Pane mapPaneContainer = new Pane();

	private final Pane statusPaneContainer = new Pane();

	private final StackPane loginPaneContainer = new StackPane();

	public GameExplorerPane() {
		init();
	}

	public ExplorerCommunicator getCommunicator() {
		return communicator;
	}

	private void init() {
		initStatusPane();
		initDataPane();
		initMapPane();

		initGamePane();
		initDividePane();

		initLoginPane();

		startDataThread();
	}

	private void initDataPane() {
		DataPane dataPane = new DataPane(communicator);

		HBox.setHgrow(dataPaneContainer, Priority.SOMETIMES);
		dataPane.prefHeightProperty().bind(dataPaneContainer.heightProperty());
		dataPane.prefHeightProperty().bind(dataPaneContainer.heightProperty());
		dataPane.minWidthProperty().bind(dataPaneContainer.widthProperty());
		dataPane.maxWidthProperty().bind(dataPaneContainer.widthProperty());

		dataPaneContainer.getChildren().add(dataPane);
	}

	private void initDividePane() {
		AnchorPane.setTopAnchor(divideContainer, 0.0);
		AnchorPane.setBottomAnchor(divideContainer, 0.0);
		AnchorPane.setLeftAnchor(divideContainer, 0.0);
		AnchorPane.setRightAnchor(divideContainer, 0.0);
		divideContainer.setFillWidth(true);

		divideContainer.getChildren().addAll(gamePaneContainer, statusPaneContainer);

		this.getChildren().add(divideContainer);
	}

	private void initGamePane() {
		VBox.setVgrow(gamePaneContainer, Priority.SOMETIMES);
		gamePaneContainer.setFillHeight(true);

		gamePaneContainer.getChildren().addAll(mapPaneContainer, dataPaneContainer);
	}

	private void initGridPane() {
		ColumnConstraints column1 = new ColumnConstraints();
		column1.setPercentWidth(50);
		ColumnConstraints column2 = new ColumnConstraints();
		column2.setPercentWidth(50);
		root.getColumnConstraints().addAll(column1, column2);
	}

	private void initLoginPane() {
		LoginPane loginPane = new LoginPane(communicator);

		fill(loginPaneContainer, 0.0);
		loginPaneContainer.visibleProperty().bind(loginPane.visibleProperty());
		loginPane.minWidthProperty().bind(loginPaneContainer.widthProperty());
		loginPane.minHeightProperty().bind(loginPaneContainer.heightProperty());
		loginPane.maxWidthProperty().bind(loginPaneContainer.widthProperty());
		loginPane.maxHeightProperty().bind(loginPaneContainer.heightProperty());

		loginPaneContainer.getChildren().add(loginPane);

		this.getChildren().add(loginPaneContainer);
	}

	private void initMapPane() {
		MapPane mapPane = new MapPane(communicator);

		HBox.setHgrow(mapPaneContainer, Priority.SOMETIMES);
		mapPane.minHeightProperty().bind(mapPaneContainer.heightProperty());
		mapPane.maxHeightProperty().bind(mapPaneContainer.heightProperty());
		mapPane.minWidthProperty().bind(mapPaneContainer.widthProperty());
		mapPane.maxWidthProperty().bind(mapPaneContainer.widthProperty());
		mapPaneContainer.minWidthProperty().bind(divideContainer.widthProperty().divide(2));
		mapPaneContainer.prefWidthProperty().bind(divideContainer.widthProperty().divide(2));
		mapPaneContainer.maxWidthProperty().bind(divideContainer.widthProperty().divide(2));

		mapPaneContainer.getChildren().add(mapPane);
	}

	private void initStatusPane() {
		StatusPane statusPane = new StatusPane(communicator);

		VBox.setVgrow(statusPaneContainer, Priority.ALWAYS);
		statusPane.minWidthProperty().bind(statusPaneContainer.widthProperty());
		statusPane.maxWidthProperty().bind(statusPaneContainer.widthProperty());
		statusPaneContainer.minHeightProperty().bind(statusPane.minHeightProperty());
		statusPaneContainer.maxHeightProperty().bind(statusPane.maxHeightProperty());
		statusPane.prefHeightProperty().bind(statusPaneContainer.heightProperty());

		statusPaneContainer.getChildren().add(statusPane);
	}

	private void startDataThread() {
		communicator.startDataThread();
	}
}
