package com.tygron.tools.explorer.gui;

import javafx.application.Platform;
import javafx.scene.layout.AnchorPane;
import com.tygron.tools.explorer.logic.ExplorerCommunicator;

public class GameExplorerSubPane extends AnchorPane {

	private ExplorerCommunicator communicator;

	public GameExplorerSubPane(ExplorerCommunicator communicator) {
		communicator.registerPane(this);
		setCommunicator(communicator);
	}

	public ExplorerCommunicator getCommunicator() {
		return communicator;
	}

	public void processUpdate() {
	}

	private void setCommunicator(ExplorerCommunicator communicator) {
		this.communicator = communicator;
	}

	public void setInnerStatus(String state) {
		communicator.setStatus(state);
	}

	public void setStatus(final String state) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				setInnerStatus(state);
			}
		});
	}
}
