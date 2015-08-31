package com.tygron.pub.examples.player;

import java.rmi.UnexpectedException;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import com.tygron.pub.api.connector.ExtendedDataConnector;
import com.tygron.pub.api.connector.modules.PlayerModule;
import com.tygron.pub.api.data.UpdateMonitor;
import com.tygron.pub.api.data.item.Building;
import com.tygron.pub.api.data.item.Popup;
import com.tygron.pub.api.data.item.Setting;
import com.tygron.pub.api.enums.ClientType;
import com.tygron.pub.api.enums.GameMode;
import com.tygron.pub.api.enums.MapLink;
import com.tygron.pub.api.listeners.UpdateListenerInterface;
import com.tygron.pub.examples.utilities.ExampleGame;
import com.tygron.pub.examples.utilities.ExampleSettings;
import com.tygron.pub.utils.DataUtils;
import com.tygron.pub.utils.JsonUtils;
import com.tygron.pub.utils.StringUtils;
import com.tygron.pub.utils.ValueUtils;

/**
 * This example simulates 2 players, a municipality and the SSH. The SSH will attempt to randomly place
 * buildings throughout the map. Whenever planning a building succeeds, it processes the resulting popups
 * until the building is constructed or the requestis denied by the municipality. The municipality waits for
 * permit requests, and when a request is made which fits the zoning plan, it approves it. Otherwise, it
 * denies it.
 *
 * This requests demonstrates directed listening and handeling of updates, and the primary part of the process
 * of placing buildings.
 * @author Rudolf
 *
 */

public class PlanMultipleBuildingsExample {

	private static final ExtendedDataConnector adminConnector = new ExtendedDataConnector();
	private static final ExtendedDataConnector municipalConnector = new ExtendedDataConnector();
	private static final ExtendedDataConnector SSHConnector = new ExtendedDataConnector();

	private static final PlayerModule municipalPlayer = new PlayerModule(municipalConnector);
	private static final PlayerModule SSHPlayer = new PlayerModule(SSHConnector);

	private static final UpdateMonitor municipalMonitor = new UpdateMonitor(municipalConnector);
	private static final UpdateMonitor SSHMonitor = new UpdateMonitor(SSHConnector);

	private static boolean constructing = false;
	private static int currentConstructingPopup = StringUtils.NOTHING;

	private static void checkPopupsAndApprovePermits(Map<String, Map<Integer, Map<?, ?>>> items) {
		// If popups have been received, we can iterate through them
		Map<Integer, Map<?, ?>> popups = items.get("POPUPS");
		if (popups != null) {
			for (Entry<Integer, Map<?, ?>> entry : popups.entrySet()) {
				Popup popup = null;
				try {
					popup = DataUtils.castToItemObject(entry.getValue(), Popup.class);
					if (popup == null) {
						continue;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				// Is the popup meant for the municipality?
				if (!popup.isVisibleToStakeholder(municipalPlayer.getStakeholderID())) {
					continue;
				}

				// Is is a popup pertaining to a building waiting for a permit?
				boolean buildingAwaitingPermit = false;
				// Is a change in zoning required?
				boolean zoningRequest = false;
				// Is the building the SSH's?
				boolean SSHRequest = false;

				if (popup.getLinkType().equals(MapLink.BUILDINGS.name())) {
					Building building = municipalPlayer.getBuilding(popup.getLinkID());
					if (building.getOwnerID() == SSHPlayer.getStakeholderID()) {
						SSHRequest = true;
					}

					if (building.getState().equals("REQUEST_CONSTRUCTION_APPROVAL")) {
						buildingAwaitingPermit = true;
					}
					if (building.getState().equals("REQUEST_ZONING_APPROVAL")) {
						buildingAwaitingPermit = true;
						zoningRequest = true;
					}
				}
				// Not a building waiting for permission? Then skip it.
				if (!buildingAwaitingPermit) {
					continue;
				}

				// Answer yes, unless it's a requested change in zoning
				String answer = "yes";
				if (zoningRequest) {
					// if (!SSHRequest) {
					answer = "no";
					// }
				}

				// Answer it!
				municipalPlayer.popupAnswerPopup(popup, answer);
				System.out.println("Answering popup " + popup.getID() + " with answer "
						+ popup.getAnswerID(answer) + " (" + answer + ")");
			}
		}
	}

	private static void checkPopupsAndInitiateConstructions(Map<String, Map<Integer, Map<?, ?>>> items) {
		// If popups have been received, we can iterate through them
		Map<Integer, Map<?, ?>> popups = items.get("POPUPS");
		if (popups != null) {
			for (Entry<Integer, Map<?, ?>> entry : popups.entrySet()) {
				Popup popup = null;
				try {
					popup = DataUtils.castToItemObject(entry.getValue(), Popup.class);
					if (popup == null) {
						continue;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				// Is the popup meant for the SSH?
				if (!popup.isVisibleToStakeholder(SSHPlayer.getStakeholderID())) {
					continue;
				}

				// Get the appropriate answer?
				String answer = StringUtils.EMPTY;
				long date = StringUtils.NOTHING;
				if (popup.getLinkType().equals(MapLink.BUILDINGS.name())) {

					// Check it relates to an existing building, and that it's the SSH's building.
					Building building = municipalPlayer.getBuilding(popup.getLinkID());
					if (building == null) {
						continue;
					}
					if (building.getOwnerID() != SSHPlayer.getStakeholderID()) {
						continue;
					}

					String buildingState = building.getState();
					if (buildingState.equals("WAITING_FOR_DATE")) {
						// It is a newly planned building. This means that if we wish to build,
						constructing = true;
						answer = "yes";
						date = 1000;

						System.out.println("New building at: " + building.getPolygons());

					} else if (buildingState.equals("CONSTRUCTION_APPROVED")) {
						answer = "confirm";
					} else if (buildingState.equals("CONSTRUCTION_DENIED")) {
						answer = "ok";
						constructing = false;
					} else if (buildingState.equals("CONSTRUCTING")) {
						currentConstructingPopup = popup.getID();
						Double[] popupPoints = popup.getPoint();
						SSHPlayer.ping(popupPoints[0].intValue(), popupPoints[1].intValue());
					}

				}
				if (answer.equals(StringUtils.EMPTY)) {
					continue;
				}

				// answer with the answer and either with the date or without
				if (date != StringUtils.NOTHING) {
					SSHPlayer.popupAnswerPopupWithDate(popup, answer, date);
					System.out.println("Answering popup " + popup.getID() + " with answer "
							+ popup.getAnswerID(answer) + " and date " + date);
				} else {
					SSHPlayer.popupAnswerPopup(popup, answer);
					System.out.println("Answering popup " + popup.getID() + " with answer "
							+ popup.getAnswerID(answer));
				}
			}
		}
	}

	private static void checkPopupsAndProcessDeletedPopups(Map<String, Map<Integer, Map<?, ?>>> deletes) {
		// Check all popups which are deleted
		Map<Integer, Map<?, ?>> popups = deletes.get("POPUPS");
		if (popups != null) {
			for (Entry<Integer, Map<?, ?>> entry : popups.entrySet()) {
				// Transform the map to a proper object, so we can interact with it. Note that because it's a
				// deleted object, it really only has an ID.
				Popup popup = null;
				try {
					popup = DataUtils.castToItemObject(entry.getValue(), Popup.class);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (popup == null) {
					continue;
				}

				// If the popup that's dissapeared was our "constructing" popup, it means our building is done
				if (currentConstructingPopup == popup.getID()) {
					currentConstructingPopup = StringUtils.NOTHING;
					constructing = false;
					System.out.println("Construction complete");
				}
			}
		}
	}

	private static void createPermitListenerForMunicipality() {
		// The municipality will get a listener, only approving permits
		UpdateListenerInterface listener = new UpdateListenerInterface() {
			@Override
			public void update(Map<String, Map<Integer, Map<?, ?>>> items,
					Map<String, Map<Integer, Map<?, ?>>> deletes) {
				checkPopupsAndApprovePermits(items);
			}
		};
		municipalMonitor.addListener(listener, MapLink.POPUPS.toString());
	}

	private static void createPopupListenerForSSH() {
		// The SSH will get a listener, to process its popups, and to know when certain popups are gone.
		UpdateListenerInterface listener = new UpdateListenerInterface() {
			@Override
			public void update(Map<String, Map<Integer, Map<?, ?>>> items,
					Map<String, Map<Integer, Map<?, ?>>> deletes) {
				checkPopupsAndInitiateConstructions(items);
				checkPopupsAndProcessDeletedPopups(deletes);
			}
		};
		SSHMonitor.addListener(listener, MapLink.POPUPS.toString());
	}

	public static void main(String[] args) {
		startClients();
		createPermitListenerForMunicipality();
		createPopupListenerForSSH();
		startUpdateMonitors();
		startGame();
		planRandomBuildings();
		zzWaitUntilClosed();
	}

	private static void planRandomBuildings() {
		// The main loop of the SSH, planning buildings randomly
		try {
			Random rnd = new Random();
			// set the size for the buildings we wish to place, and retrieve the size of the map
			int size = 8;
			int mapSize = Integer.parseInt(JsonUtils.mapJsonToType(
					SSHConnector.getDataFromServerSession(
							MapLink.SETTINGS.itemUrl(ValueUtils.SETTING_MAP_SIZE)).getContent(),
					Setting.class).getValue());

			new Thread(new Runnable() {
				@Override
				public void run() {
					try {

						int x = 0;
						int y = 0;

						while (true) {
							if (constructing) {
								System.out.print(".");
								Thread.sleep(5000);
								continue;
							}

							// determine a random location on the map
							x = rnd.nextInt(mapSize - size);
							y = rnd.nextInt(mapSize - size);

							// create a polygon definition. Based on the location we determined randomly, and
							// the size.
							// String newMultiPolygon =
							// "MULTIPOLYGON (((%d.0 %d.0, %d.0 %d.0, %d.0 %d.0, %d.0 %d.0, %d.0 %d.0)))";

							String newMultiPolygon = "{ \"type\" : \"MultiPolygon\", \"coordinates\" : [ [ [ [ %d.0 %d.0 ], [ %d.0, %d.0 ], [ %d.0, %d.0 ], [ %d.0, %d.0 ], [ %d.0, %d.0 ] ] ] ] }";
							newMultiPolygon = String.format(newMultiPolygon, x, y, x, y + size, x + size, y
									+ size, x + size, y, x, y);

							// plan a building in the location we chose
							SSHPlayer.buildingPlanConstruction(ExampleGame.BUILDING1.FUNCTION,
									ExampleGame.BUILDING1.FLOORS, newMultiPolygon);
							Thread.sleep(100);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		System.out.println("Begun planning buildings");
	}

	private static void startClients() {

		boolean success = true;

		// The admin will start the session
		try {
			success = adminConnector.startSessionAndJoin(ExampleSettings.SERVER, ExampleSettings.USERNAME,
					ExampleSettings.PASSWORD, ExampleGame.GAME, ExampleGame.LANGUAGE,
					GameMode.MULTI_PLAYER.toString(), ClientType.ADMIN.toString(),
					ExampleSettings.CLIENT_ADDRESS, ExampleSettings.CLIENT_NAME);
		} catch (UnexpectedException e) {
			System.err.println("Failed to start or connect to session (adminConnector).");
			e.printStackTrace();
			success = false;
		}

		// The municipality player will join
		try {
			success = municipalConnector.joinSession(ExampleSettings.SERVER, ExampleSettings.USERNAME,
					ExampleSettings.PASSWORD, ClientType.VIEWER.toString(), adminConnector.getServerSlot(),
					ExampleSettings.CLIENT_ADDRESS, ExampleSettings.CLIENT_NAME);
		} catch (UnexpectedException e) {
			System.err.println("Failed to start or connect to session (municipalConnector).");
			e.printStackTrace();
			success = false;
		}
		municipalPlayer.selectStakeholder(ExampleGame.STAKEHOLDER_MUNICIPALITY);

		// The SSH player will join
		try {
			success = SSHConnector.joinSession(ExampleSettings.SERVER, ExampleSettings.USERNAME,
					ExampleSettings.PASSWORD, ClientType.VIEWER.toString(), adminConnector.getServerSlot(),
					ExampleSettings.CLIENT_ADDRESS, ExampleSettings.CLIENT_NAME);
		} catch (UnexpectedException e) {
			System.err.println("Failed to start or connect to session (municipalConnector).");
			e.printStackTrace();
			success = false;
		}
		SSHPlayer.selectStakeholder(ExampleGame.STAKEHOLDER_SSH);

		if (!success) {
			System.exit(-1);
		}
		System.out.println("Clients started");
		System.out.println(adminConnector.getBrowserURL());
	}

	private static void startGame() {
		adminConnector.allowGameInteraction(true);
	}

	private static void startUpdateMonitors() {
		// We start the update monitors in separate threads, and only have them listen for popups.
		new Thread(new Runnable() {
			@Override
			public void run() {
				municipalMonitor.startListening(Arrays.asList("POPUPS"));
			}
		}).start();

		new Thread(new Runnable() {
			@Override
			public void run() {
				SSHMonitor.startListening(Arrays.asList("POPUPS"));
			}
		}).start();
		System.out.println("Update monitors started");
	}

	private static void zzWaitUntilClosed() {
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

				System.out.println("Instructing the updateMonitors to stop listening.");
				municipalMonitor.stopListening();
				SSHMonitor.stopListening();

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
