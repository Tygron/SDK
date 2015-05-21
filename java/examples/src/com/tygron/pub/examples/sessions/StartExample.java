package com.tygron.pub.examples.sessions;

import com.tygron.pub.api.connector.DataConnector;
import com.tygron.pub.api.connector.DataPackage;
import com.tygron.pub.examples.utilities.ExampleGame;
import com.tygron.pub.examples.utilities.ExampleSettings;

/**
 * This api-based example triggers an event on the Tygron Engine. It starts the project "Climategame" in
 * Single Player mode, and prints the serverslot in which the session was started.
 *
 * It is important to note that this event takes a while to complete, thus the thread blocks for up to 20
 * seconds.
 * @author Rudolf
 *
 */

public class StartExample {

	public static void main(String[] args) {
		// We create an object which will facilitate our communication with the engine, and provide it with
		// the required credentials.
		DataConnector dataConnector = new DataConnector();
		dataConnector.setUsernameAndPassword(ExampleSettings.USERNAME, ExampleSettings.PASSWORD);

		// Prepare an absolute URL indicating the event we wish to trigger. Specifically, we wish to start a
		// new session on the server, so we call "START_NEW_SESSION"
		String target = ExampleSettings.SERVER
				+ "/api/services/event/IOServicesEventType/START_NEW_SESSION?f=JSON";

		// We send a request to the url we created, with the parameters expected by the event. The last two
		// are optional, so we make those null.
		DataPackage data = dataConnector.sendData(target, "SINGLE_PLAYER", ExampleGame.GAME,
				ExampleGame.LANGUAGE, null, null);

		// We receive a number, indicating the server slot in which our session has started.
		String receivedString = data.getContent();
		System.out.println(receivedString);
	}
}
