package com.tygron.pub.examples.api;

import java.rmi.UnexpectedException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import com.tygron.pub.api.connector.ExtendedDataConnector;
import com.tygron.pub.api.connector.modules.PlayerModule;
import com.tygron.pub.api.data.UpdateMonitor;
import com.tygron.pub.api.enums.ClientType;
import com.tygron.pub.api.enums.GameMode;
import com.tygron.pub.api.listeners.UpdateListenerInterface;
import com.tygron.pub.examples.settings.ExampleGame;
import com.tygron.pub.examples.settings.ExampleSettings;

/**
 * This api-based example performs all necessary steps to connect to a game. It then opens another connection,
 * where it will select the municipality as stakeholder. The municipality will listen for new permit requests,
 * and automatically approve them.
 *
 * This example will show a popup. When the popup is closed, the session will be closed. To observe the
 * effects of this example, it is recommended to join the session as the SSH, and construct student housings.
 *
 * It is important to note that starting a session takes a while to complete, thus the thread blocks for up to
 * 20 seconds.
 * @author Rudolf
 *
 */
public class UpdateListeningExample {
	public static void main(String[] args) {

		// We start 2 dataconnectors. 1 for the admin, and the second for a player (the municipality).
		final ExtendedDataConnector adminConnector = new ExtendedDataConnector();
		final ExtendedDataConnector municipalConnector = new ExtendedDataConnector();

		boolean success = true;

		// The admin will start the session
		try {
			success = adminConnector.startSessionAndJoin(ExampleSettings.SERVER, ExampleSettings.USERNAME,
					ExampleSettings.PASSWORD, ExampleGame.GAME, ExampleGame.LANGUAGE,
					GameMode.MULTI_PLAYER.toString(), ClientType.ADMIN.toString(),
					ExampleSettings.CLIENT_ADDRESS, ExampleSettings.CLIENT_NAME);
		} catch (UnexpectedException e) {
			e.printStackTrace();
			success = false;
		}

		if (!success) {
			System.out.println("Failed to start or connect to session (adminConnector).");
			System.exit(-1);
		}

		System.out.println("Server slot: " + adminConnector.getServerSlot());
		System.out.println("Server token: " + adminConnector.getServerToken());
		System.out.println("Client token (admin): " + adminConnector.getClientToken());

		// Start the game
		adminConnector.allowGameInteraction(true);

		// The municipality player will join
		try {
			success = municipalConnector.joinSession(ExampleSettings.SERVER, ExampleSettings.USERNAME,
					ExampleSettings.PASSWORD, ClientType.VIEWER.toString(), adminConnector.getServerSlot(),
					ExampleSettings.CLIENT_ADDRESS, ExampleSettings.CLIENT_NAME);
		} catch (UnexpectedException e) {
			e.printStackTrace();
			success = false;
		}

		if (!success) {
			System.out.println("Failed to connect to session (municipalConnector).");
			System.exit(-1);
		}
		System.out.println("Client token (player): " + municipalConnector.getClientToken());
		System.out.println(adminConnector.getBrowserURL());

		// Create a player for the municipality
		PlayerModule player = new PlayerModule(municipalConnector);

		// Select a stakeholder
		try {
			success = player.selectStakeholder(ExampleGame.STAKEHOLDER_MUNICIPALITY);
		} catch (Exception e) {
			e.printStackTrace();
			success = false;
		}

		if (!success) {
			System.err.println("We failed to select a stakeholder");
			success = adminConnector.closeConnectedSession();

			if (!success) {
				System.out.println("Failed to close session correctly.");
				System.exit(-1);
			} else {
				System.out.println("Session closed");
			}
			System.exit(0);
		}

		// We'll set up a listener. Whenever a popup appears for the municipality, it will attempt to answer
		// positively.
		UpdateListenerInterface listener = new UpdateListenerInterface() {

			// We're keeping an overview of the popups we've answered, until we see them deleted
			List<Integer> answeredPopups = new LinkedList<Integer>();

			@Override
			public void update(Map<String, Map<Integer, Map<?, ?>>> items,
					Map<String, Map<Integer, Map<?, ?>>> deletes) {
				// If popups have been received, we can iterate through them
				Map<Integer, Map<?, ?>> popups = items.get("POPUPS");
				if (popups != null) {
					for (Entry<Integer, Map<?, ?>> popup : popups.entrySet()) {
						try {
							// For each popup, check if it's for us, and if we can answer it with "yes".
							Integer popupID = popup.getKey();
							String answerID = null;
							boolean visible = ((List) popup.getValue().get("visibleForActorIDs"))
									.contains(ExampleGame.STAKEHOLDER_MUNICIPALITY);
							boolean interaction = popup.getValue().get("type").equals("INTERACTION");

							for (Map<String, Object> answer : (List<Map<String, Object>>) popup.getValue()
									.get("answers")) {
								if (answer.get("contents").toString().toLowerCase().equals("yes")) {
									answerID = answer.get("id").toString();
									break;
								}
							}

							// This popup is for the municipality, and has a "yes" answer, so lets answer.
							if (visible && interaction && answerID != null) {
								Object object = municipalConnector.sendDataToServerSession(
										"PlayerEventType/POPUP_ANSWER",
										Integer.toString(ExampleGame.STAKEHOLDER_MUNICIPALITY),
										Integer.toString(popupID), answerID);
								System.out.println("Answering popup " + popupID + " with answer " + answerID);

								// Remember that we answered this popup.
								answeredPopups.add(popupID);
							}

						} catch (ClassCastException e) {
							System.err.println("Failed to cast part of the popup properly.");
							e.printStackTrace();
						}
					}
				}

				// We'll also itterate through the popups that have been deleted
				popups = deletes.get("POPUPS");
				if (popups != null) {
					for (Integer popupId : popups.keySet()) {
						if (answeredPopups.contains(popupId)) {
							System.out.println("We've previously answered popup " + popupId
									+ ", which has now been deleted");
							answeredPopups.remove(popupId);
						}
					}
				}
			}
		};

		// Now we have a listener, now it just needs something to listen to.
		final UpdateMonitor updateMonitor = new UpdateMonitor();

		// We let the updatemonitor perform its operations from the perspective of the municipality player. It
		// is, after all, effectively the municipality monitoring.
		updateMonitor.setDataConnector(municipalConnector);

		// When an update occurs, the listener we specified earlier must be informed.
		updateMonitor.addListener(listener);

		// In a separate thread, we start listening, and indicate we only want to listen to the popups.
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					updateMonitor.startListening(Arrays.asList("POPUPS"));
				} catch (Exception e) {
					// When the updatemonitor is suddenly facing a closed session, an expection will likely
					// occur. This can be prevented by invoking "stopListening()" before the session is
					// closed, but if it isn't, the exception will be caught here.
					System.out.println("Listening has caused the following exception: ");
					e.printStackTrace();
				}
			}
		}).start();

		// At this point, the updateMonitor is listening for updates, and the listener will be informed when
		// new updates occur.

		// You can now join the session as another stakeholder and request permits for constructions. The
		// municipality will always and immediately approve.

		// We keep this example running until we want it to close, but to close it neatly it should be closed
		// by the admin. We'll let a popup window appear. When it is closed, we attempt to close the session.
		new JFXPanel();
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Running example");
				alert.setHeaderText(null);
				alert.setContentText("Close me to end the example");
				alert.showAndWait();

				System.out.println("Instructing the updateMonitor to stop listening.");
				updateMonitor.stopListening();

				System.out.println("Closing sessions...");
				boolean success = adminConnector.closeConnectedSession();

				if (!success) {
					System.out.println("Failed to close session correctly.");
					System.exit(-1);
				} else {
					System.out.println("Session closed");
				}
				System.exit(0);
			}
		});

	}
}
