package com.tygron.tools.explorer.gui;

import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import com.tygron.pub.utils.StringUtils;
import com.tygron.tools.explorer.logic.ExplorerCommunicator;

public class StatusPane extends GameExplorerSubPane {

	HBox hbox = new HBox();
	StackPane stackPane = new StackPane();
	Pane leftBufferPane = new Pane();
	Text statusText = new Text();

	public StatusPane(ExplorerCommunicator communicator) {
		super(communicator);
		this.setMinHeight(24.0);
		this.setMaxHeight(32.0);

		this.getChildren().add(hbox);

		leftBufferPane.minWidthProperty().bind(this.heightProperty().divide(2));
		hbox.getChildren().add(leftBufferPane);

		stackPane.setAlignment(Pos.CENTER_LEFT);
		hbox.getChildren().add(stackPane);

		statusText.setTextOrigin(VPos.CENTER);
		stackPane.getChildren().add(statusText);
		HBox.setHgrow(stackPane, Priority.ALWAYS);

		GameExplorerPane.fill(hbox, 0.0);
		setStyle("-fx-background-color: #cccccc;");
		setStatus(StringUtils.EMPTY);
	}

	@Override
	public void setInnerStatus(String status) {
		statusText.setText(status);
	}
}
