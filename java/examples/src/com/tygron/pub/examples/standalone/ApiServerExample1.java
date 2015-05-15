package com.tygron.pub.examples.standalone;

import java.net.URL;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import com.tygron.pub.examples.standalone.apiserver.ExampleServerConfig1;

/**
 * This example serves as a demonstration on how to interact with an API indicator. This example is more
 * involved to run, because the following is required to make this example run:
 * <ol>
 * <li>You must have a game with an API indicator</li>
 * <li>You must have a publicly reachable server where this code can run</li>
 * </ol>
 *
 * To run this example locally, but have it available to the Tygron Engine, you can use a service such as <a
 * href="http://www.ngrok.io">ngrok.io</a>.
 *
 * To have a game with an API indicator, create a new game or load an existing game in the editor in the
 * Tygron Engine client. Add a new "API Indicator". Set the server address for the indicator to the address of
 * your (now publicly available) server, and set the target for any value.
 *
 * When the server is prompted for a calculation for the indicator, the server is free to access the game,
 * retrieve information, and perform its calculations. When complete, the server needn't respond to the
 * request, but can update the indicator by making its own POST request to update the API indicator.
 *
 * See ExampleServerConfig1's runModel function for more insight into how this example works.
 *
 * @author Maxim
 *
 */

public class ApiServerExample1 {
	private static String MY_URL = "http://localhost";

	public static void main(String[] args) {

		if (args.length > 0) {
			MY_URL = args[0];
		}

		System.out.println("My Address: " + MY_URL);
		System.out.println();

		/**
		 * Start Grizzly server
		 */
		new ApiServerExample1();

		/**
		 * Do not die
		 */
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
	}

	public ApiServerExample1() {

		try {

			URL url = new URL(MY_URL);
			ResourceConfig config = new ResourceConfig(ExampleServerConfig1.class);
			HttpServer server = GrizzlyHttpServerFactory.createHttpServer(url.toURI(), config);
			server.start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
