package com.tygron.pub.examples.api;

import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.tygron.pub.api.connector.DataPackage;
import com.tygron.pub.api.connector.ExtendedDataConnector;
import com.tygron.pub.api.connector.enums.ClientType;
import com.tygron.pub.api.connector.enums.GameMode;
import com.tygron.pub.examples.settings.ExampleGame;
import com.tygron.pub.examples.settings.ExampleSettings;
import com.tygron.pub.utils.JsonUtils;

/**
 * This api-based example convenience functions to perform all necessary steps to connect to a game. It then
 * prints the amount of buildings present in the game, as well as a list of available indicators, and closes
 * its connection to the session (and with it, the session itself).
 *
 * It is important to note that starting a session takes a while to complete, thus the thread blocks for up to
 * 20 seconds.
 * @author Rudolf
 *
 */

public class StartSessionExample2 {

	public static void main(String[] args) {
		ExtendedDataConnector dataConnector = new ExtendedDataConnector();
		boolean success = true;

		// Start a session on the server and connect to it.
		try {
			success = dataConnector.startSessionAndJoin(ExampleSettings.SERVER, ExampleSettings.USERNAME,
					ExampleSettings.PASSWORD, ExampleGame.GAME, ExampleGame.LANGUAGE,
					GameMode.MULTI_PLAYER.toString(), ClientType.ADMIN.toString(),
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

		// Retrieve the size of the list of buildings, and output it.
		DataPackage data = dataConnector.getDataFromServerSession("buildings/size");
		String receivedString = data.getContent();
		System.out.println("Amount of buildings in game: " + receivedString);

		// Retrieve the list of indicators.
		data = dataConnector.getDataFromServerSession("indicators/");

		// Map the retrieved json data to a collection of maps.
		List<Map<String, Map<String, Object>>> listedIndicators = new ArrayList<Map<String, Map<String, Object>>>();
		JsonUtils.mapJsonIntoObject(data.getContent(), listedIndicators);

		// Itterate through all the retrieved maps, each representing a single indicator.
		for (Map<String, Map<String, Object>> indicator : listedIndicators) {
			try {

				// The indicators are contained in a second layer of maps, the key to which varies.
				Map<String, Object> innerIndicator = null;
				for (Map<String, Object> value : indicator.values()) {
					// Since there's only 1 indicator per indicator, we can grab the first value in the map we
					// see and continue.
					innerIndicator = value;
					break;
				}

				// Print the name of the indicator we've found.
				if (innerIndicator != null) {
					String name = (String) innerIndicator.get("name");
					System.out.println(name);
				}

			} catch (ClassCastException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}

		// End our connection to the session. This also kills the session, unless another client joined in the
		// meantime.
		success = dataConnector.closeConnectedSession();

		if (!success) {
			System.out.println("Failed to close session correctly.");
			System.exit(-1);
		} else {
			System.out.println("Session closed");
		}
		System.exit(0);
	}
}
