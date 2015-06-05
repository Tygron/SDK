package com.tygron.tools.explorer.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import com.tygron.pub.logger.Log;
import com.tygron.tools.explorer.logic.ExplorerCommunicator;

public class ExplorerApplication extends Application {

	private ExplorerCommunicator communicator;
	private GameExplorerPane root;

	@Override
	public void start(Stage stage) throws Exception {
		stage.setTitle("Explorer Application");

		this.root = new GameExplorerPane();
		communicator = root.getCommunicator();

		stage.setScene(new Scene(root, 800, 600));
		stage.show();

		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				try {
					Log.info("Stopping...");
					communicator.stopDataThread();
					stop();
				} catch (Exception e) {
					e.printStackTrace();
					Platform.exit();
				}
			}
		});

	}
}
