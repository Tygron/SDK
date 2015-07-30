package com.tygron.tools.explorer.gui;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javax.xml.ws.http.HTTPException;
import com.tygron.pub.api.data.misc.JoinableSessionObject;
import com.tygron.pub.exceptions.AuthenticationException;
import com.tygron.pub.exceptions.NoSuchServerException;
import com.tygron.pub.exceptions.PageNotFoundException;
import com.tygron.pub.logger.Log;
import com.tygron.pub.utils.SettingsUtils;
import com.tygron.pub.utils.SettingsUtils.TygronCredential;
import com.tygron.pub.utils.StringUtils;
import com.tygron.tools.explorer.logic.ExplorerCommunicator;

public class LoginPane extends GameExplorerSubPane {

	private class ComboBoxObject {
		String displayString = StringUtils.EMPTY;
		String valueString = StringUtils.EMPTY;

		private ComboBoxObject(String display, String value) {
			this.displayString = display;
			this.valueString = value;
		}

		public String getValue() {
			return valueString;
		}

		@Override
		public String toString() {
			return displayString;
		}
	}

	private final static int MARGIN = 25;
	private StackPane loginContainer = new StackPane();
	private AnchorPane loginPane = new AnchorPane();
	private VBox gridContainer = new VBox();

	private GridPane loginGridPane = new GridPane();
	private TextField serverField = new TextField();
	private TextField usernameField = new TextField();
	private PasswordField passwordField = new PasswordField();

	private GridPane sessionGridPane = new GridPane();
	private TextField projectField = new TextField();
	private TextField slotField = new TextField();
	private ComboBox<String> projectBox = new ComboBox<String>();

	private ComboBox<String> slotBox = new ComboBox<String>();
	private Button loginButton = new Button("Log in");
	private Button startButton = new Button("Start");
	private Button joinButton = new Button("Join");

	private Text statusText = new Text();

	private String defaultProject = StringUtils.EMPTY;
	private String defaultSlot = StringUtils.EMPTY;

	public LoginPane(ExplorerCommunicator communicator) {
		super(communicator);
		setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
		loginPane.setStyle("-fx-background-color: rgba(196, 196, 196, 1);");
		GameExplorerPane.fill(loginContainer, 50.0);

		loginGridPane.setAlignment(Pos.CENTER);
		loginGridPane.setHgap(10);
		loginGridPane.setVgap(10);
		loginGridPane.setPadding(new Insets(25, 25, 25, 25));

		int row = 1;

		addToGrid(loginGridPane, row++, "Server", serverField);
		addToGrid(loginGridPane, row++, "Username", usernameField);
		addToGrid(loginGridPane, row++, "Password", passwordField, loginButton);

		EventHandler<KeyEvent> enterHandler = new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent e) {
				if (e.getCode().equals(KeyCode.ENTER)) {
					loginButton.fire();
				}
			}
		};

		serverField.setOnKeyPressed(enterHandler);
		usernameField.setOnKeyPressed(enterHandler);
		passwordField.setOnKeyPressed(enterHandler);

		row = 1;

		sessionGridPane.setAlignment(Pos.CENTER);
		sessionGridPane.setHgap(10);
		sessionGridPane.setVgap(10);
		sessionGridPane.setPadding(new Insets(25, 25, 25, 25));

		addToGrid(sessionGridPane, row++, "Project", projectBox, startButton);
		addToGrid(sessionGridPane, row++, "Slot", slotBox, joinButton);

		sessionGridPane.setVisible(false);

		loginPane.minHeightProperty().bind(gridContainer.heightProperty().add(MARGIN * 2));
		loginPane.maxHeightProperty().bind(loginContainer.heightProperty());
		loginPane.prefHeightProperty().bind(gridContainer.heightProperty().add(MARGIN * 2));

		loginPane.setMinWidth(150);
		loginPane.setMaxWidth(450);
		loginPane.prefWidthProperty().bind(loginContainer.widthProperty().divide(2));

		AnchorPane.setLeftAnchor(gridContainer, new Double(MARGIN));
		AnchorPane.setRightAnchor(gridContainer, new Double(MARGIN));
		AnchorPane.setTopAnchor(gridContainer, new Double(MARGIN));

		gridContainer.getChildren().addAll(loginGridPane, sessionGridPane);

		loginButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				buttonPressed(true, false, false);
			}
		});
		startButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				buttonPressed(false, true, false);
			}
		});
		joinButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				buttonPressed(false, false, true);
			}
		});

		statusText.setText(StringUtils.EMPTY);
		gridContainer.getChildren().add(statusText);

		loginPane.getChildren().add(gridContainer);
		loginContainer.getChildren().add(loginPane);
		getChildren().add(loginContainer);

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				loadCredentialsFromFile();
			}
		});
	}

	private void addToGrid(GridPane grid, int row, String text, Control field, Control... controls) {
		int i = 0;
		Text textLabel = new Text(text);
		grid.add(textLabel, i++, row);
		field.maxWidthProperty().bind(loginPane.widthProperty());
		field.prefWidthProperty().bind(loginPane.widthProperty());
		grid.add(field, i++, row);
		if (controls == null || controls.length == 0) {
			return;
		}
		for (Control control : controls) {
			control.minWidthProperty().bind(grid.widthProperty().divide(6));
			// button.minWidthProperty().bind(button.prefWidthProperty());
			// button.prefWidthProperty().bind(button.maxWidthProperty());
			grid.add(control, i++, row);
		}
	}

	private void buttonPressed(boolean login, boolean start, boolean join) {
		setButtonsDisabled(true);

		Thread pressedThread = new Thread() {
			@Override
			public void run() {
				try {
					storeConnectionSettings();
					connect(login, start, join);
				} catch (Exception e) {
					Log.exception(e, "Uncaught exception.");
					setStatus("Something unexpected happened");
				} finally {
					setButtonsDisabled(false);
				}
			}
		};

		pressedThread.start();
	}

	private void connect(boolean login, boolean start, boolean join) {
		boolean loggedIn = true;
		try {
			if (login) {
				loggedIn = false;
				setStatus("Attempting to log in.", true);
				loggedIn = connectLogin();
			} else if (start) {
				setStatus("Attempting to start session.", true);
				connectStartOrJoin(false);
			} else if (join) {
				setStatus("Attempting to join session.", true);
				connectStartOrJoin(true);
			}
		} catch (HTTPException e) {
			setStatus("Failed to connect due to HTTP exception.");
		} catch (IllegalArgumentException e) {
			setStatus("Invalid argument (project or slot).");
		} catch (AuthenticationException e) {
			setStatus("Invalid credentials.");
			loggedIn = false;
		} catch (NoSuchServerException e) {
			setStatus("That server does not exist.");
			loggedIn = false;
		} catch (PageNotFoundException e) {
			setStatus("The server's url is faulty.");
			loggedIn = false;
		}
		sessionGridPane.setVisible(loggedIn);
	}

	private boolean connectLogin() {
		setCredentials();

		Map<String, ?> userDetails = getCommunicator().getCurrentUser();

		List<JoinableSessionObject> sessions = null;
		sessions = getCommunicator().getJoinableProjects();
		fillSessionsList(sessions);

		Map<String, Collection<String>> projects = null;
		projects = getCommunicator().getStartableProjects((String) userDetails.get("domain"));
		fillProjectsList(projects);

		setStatus(StringUtils.EMPTY);
		return true;
	}

	private boolean connectStartOrJoin(boolean join) {
		String result = StringUtils.EMPTY;
		boolean success = false;
		if (join) {
			try {
				String slot = slotBox.getValue().split(":")[0];
				success = getCommunicator().attemptConnection(null, null, null, null, slot);
			} catch (ArrayIndexOutOfBoundsException e) {
				result = "Select a session";
			} catch (NullPointerException e) {
				result = "Select a session";
			} catch (NumberFormatException e) {
				result = "Selected slot is not valid, must be numerical";
			}
		} else {
			success = getCommunicator().attemptConnection(null, null, null,
					projectBox.getSelectionModel().getSelectedItem(), null);
		}
		if (success) {
			hideLogin();
		} else {
			setStatus("Failed to start or connect to game.");
			if (!StringUtils.isEmpty(result)) {
				setStatus("Result");
			}
		}
		return success;
	}

	private void fillProjectsList(Map<String, Collection<String>> projects) {

		List<String> projectNames = new LinkedList<String>();
		// List<String> languages = new LinkedList<String>();

		projectNames.addAll(projects.keySet());
		java.util.Collections.sort(projectNames);

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				projectBox.getItems().clear();
				for (String project : projectNames) {
					projectBox.getItems().add(project);

					// languages.clear();
					// languages.addAll(projects.get(project));
					// if (languages.size() <= 1) {
					// projectBox.getItems().add(project);
					// continue;
					// }
					// if (languages.size() > 1) {
					// java.util.Collections.sort(languages);
					// }
					// for (String language : languages) {
					// projectBox.getItems().add(project + " (" + language + ")");
					// }

				}
				if (projectBox.getItems().contains(defaultProject)) {
					projectBox.setValue(defaultProject);
				}
			}
		});
	}

	private void fillSessionsList(List<JoinableSessionObject> sessions) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				slotBox.getItems().clear();
				for (JoinableSessionObject session : sessions) {
					String name = session.getID() + ": " + session.getName() + " (" + session.getLanguage()
							+ ")";
					slotBox.getItems().add(name);
				}
				if (slotBox.getItems().contains(defaultSlot)) {
					slotBox.setValue(defaultSlot);
				}
			}
		});
	}

	public void hideLogin() {
		this.setVisible(false);
	}

	public void loadCredentialsFromFile() {
		try {
			Properties properties = SettingsUtils.loadProperties();
			serverField
					.setText(properties.getProperty(TygronCredential.SERVER.toString(), StringUtils.EMPTY));
			usernameField.setText(properties.getProperty(TygronCredential.USERNAME.toString(),
					StringUtils.EMPTY));
			passwordField.setText(properties.getProperty(TygronCredential.PASSWORD.toString(),
					StringUtils.EMPTY));

			defaultProject = properties.getProperty("GAME", StringUtils.EMPTY);
			defaultSlot = properties.getProperty("SLOT", StringUtils.EMPTY);
		} catch (IOException e) {
			setStatus("Failed to load credentials from file.");
		}
	}

	private void setButtonsDisabled(boolean disabled) {
		loginButton.setDisable(disabled);
		startButton.setDisable(disabled);
		joinButton.setDisable(disabled);
	}

	private void setCredentials() {
		getCommunicator().setCredentials(serverField.getText(), usernameField.getText(),
				passwordField.getText());
	}

	@Override
	public void setInnerStatus(String status) {
		setStatus(status, false);
	}

	private void setStatus(String status, boolean good) {
		if (good) {
			statusText.setStroke(Color.DARKGREEN);
		} else {
			statusText.setStroke(Color.CRIMSON);
		}
		statusText.setText(status);
	}

	public void showLogin() {
		statusText.setText(StringUtils.EMPTY);
		this.setVisible(true);
	}

	private void storeConnectionSettings() {
		Properties properties = new Properties();
		if (!StringUtils.isEmpty(serverField.getText())) {
			properties.setProperty(TygronCredential.SERVER.toString(), serverField.getText());
		}
		if (!StringUtils.isEmpty(usernameField.getText())) {
			properties.setProperty(TygronCredential.USERNAME.toString(), usernameField.getText());
		}
		if (!StringUtils.isEmpty(passwordField.getText())) {
			// properties.setProperty(TygronCredential.PASSWORD.toString(), passwordField.getText());
		}
		if (!StringUtils.isEmpty(projectBox.getValue())) {
			properties.setProperty("GAME", projectBox.getValue());
		}
		if (!StringUtils.isEmpty(slotBox.getValue())) {
			properties.setProperty("SLOT", slotBox.getValue());
		}
		try {
			SettingsUtils.storeProperties(SettingsUtils.DEFAULT_AUTH_FILE, properties, true, true);
		} catch (IOException e) {
			setStatus("Failed to store credentials");
		}
	}
}
