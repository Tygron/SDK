package com.tygron.tools.explorer.gui;

import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

public class MapPaneSubPane extends AnchorPane {

	public void fill(Node node, double fit) {
		AnchorPane.setBottomAnchor(node, fit);
		AnchorPane.setTopAnchor(node, fit);
		AnchorPane.setLeftAnchor(node, fit);
		AnchorPane.setRightAnchor(node, fit);
	}
}
