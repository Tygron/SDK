package com.tygron.pub.examples.api;

import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.tygron.pub.api.connector.DataPackage;
import com.tygron.pub.api.connector.ExtendedDataConnector;
import com.tygron.pub.api.enums.ClientType;
import com.tygron.pub.api.enums.GameMode;
import com.tygron.pub.examples.settings.ExampleGame;
import com.tygron.pub.examples.settings.ExampleSettings;
import com.tygron.pub.utils.JsonUtils;

/**
 * This api-based example performs all necessary steps to connect to a game. It then selects a stakeholder,
 * and will plan a number of spatial tasks in the world.
 *
 * This example does not automatically close the session. To observe the effects of this example, it is
 * recommended to join the session as the municipality.
 *
 * It is important to note that starting a session takes a while to complete, thus the thread blocks for up to
 * 20 seconds.
 * @author Rudolf
 *
 */

public class PlanActionsExample {

	public static void main(String[] args) {
		ExtendedDataConnector dataConnector = new ExtendedDataConnector();
		boolean success = true;

		// Start a session on the server and connect to it.
		try {
			success = dataConnector.startSessionAndJoin(ExampleSettings.SERVER, ExampleSettings.USERNAME,
					ExampleSettings.PASSWORD, ExampleGame.GAME, ExampleGame.LANGUAGE,
					GameMode.MULTI_PLAYER.toString(), ClientType.VIEWER.toString(),
					ExampleSettings.CLIENT_ADDRESS, ExampleSettings.CLIENT_NAME);
		} catch (UnexpectedException e) {
			e.printStackTrace();
			success = false;
		}

		if (!success) {
			System.out.println("Failed to start or connect to session.");
			System.exit(-1);
		}

		System.out.println("Server slot: " + dataConnector.getServerSlot());
		System.out.println("Server token: " + dataConnector.getServerToken());
		System.out.println("Client token: " + dataConnector.getClientToken());

		// Start the game
		dataConnector.allowGameInteraction(true);

		// Select a stakeholder
		try {
			success = dataConnector.selectStakeholder(ExampleGame.STAKEHOLDER);
		} catch (Exception e) {
			e.printStackTrace();
			success = false;
		}

		if (!success) {
			System.err.println("We failed to select a stakeholder");
			success = dataConnector.closeConnectedSession();
		}

		// Retrieve the size of the list of buildings, and output it.
		DataPackage data = dataConnector.getDataFromServerSession("buildings/size");
		String receivedString = data.getContent();
		System.out.println("Amount of buildings in game: " + receivedString);

		// plan a construction on a self-owned location
		dataConnector.planBuilding(ExampleGame.STAKEHOLDER, ExampleGame.BUILDING1.FUNCTION,
				ExampleGame.BUILDING1.FLOORS, ExampleGame.STAKEHOLDER_OWNED_LOCATIONS[0]);

		// Retrieve the size of the list of buildings, and check it against the value retrieved earlier.
		data = dataConnector.getDataFromServerSession("buildings/size");
		try {
			int first = Integer.parseInt(receivedString);
			int second = Integer.parseInt(data.getContent());
			if (first + 1 == second) {
				System.out.println("Exactly one new building present.");
			} else {
				System.out.println("We went from " + first + " to " + second + ".");
			}
		} catch (NumberFormatException e) {
			System.out.println("Either " + receivedString + " or " + data.getContent() + " is not a number.");
		}

		// A popup has now appeared prompting us to set a date to start construction

		// Retrieve the list of popups
		data = dataConnector.getDataFromServerSession("popups");
		List<Map<String, Map<String, Object>>> popups = new ArrayList<Map<String, Map<String, Object>>>();
		JsonUtils.mapJsonIntoObject(data.getContent(), popups);

		// For each popup, check if it's for us, if it's related to a building, and if it's waiting for a
		// date. If it is, answer the popup with a yes and a date.
		for (Map<String, Map<String, Object>> popupMap : popups) {

			// All information about the popup is inside a second map "PopupData"
			Map<String, Object> popup = popupMap.get("PopupData");

			try {
				String popupID = popup.get("id").toString();
				String answerID = null;
				String date = "1420113600000"; // 12:00 PM, 1-1-2015
				boolean visible = ((List) popup.get("visibleForActorIDs")).contains(ExampleGame.STAKEHOLDER);
				boolean linkType = popup.get("linkType").equals("BUILDINGS");
				boolean requiresDate = popup.get("type").equals("INTERACTION_WITH_DATE");

				// A message may have several answers. Find the one that says "Yes".
				for (Map<String, Object> answer : (List<Map<String, Object>>) popup.get("answers")) {
					if (answer.get("contents").toString().toLowerCase().equals("yes")) {
						answerID = answer.get("id").toString();
						break;
					}
				}

				// This popup meets all criteria, and has the correct sorts of answers, so we'll answer it
				if (visible && linkType && requiresDate && answerID != null) {
					dataConnector.sendDataToServerSession("PlayerEventType/POPUP_ANSWER_WITH_DATE",
							Integer.toString(ExampleGame.STAKEHOLDER), popupID, answerID, date);
				}

			} catch (ClassCastException e) {
				System.err.println("We met some unexpected values...");
			}
		}

		System.exit(0);
	}
}
