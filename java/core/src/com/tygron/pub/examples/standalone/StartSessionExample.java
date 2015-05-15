package com.tygron.pub.examples.standalone;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tygron.pub.examples.settings.ExampleGame;
import com.tygron.pub.examples.settings.ExampleSettings;

/**
 * This stand-alone example triggers an event on the Tygron Engine. It starts the project "Climategame" in
 * Single Player mode, and returns the serverslot in which the session was started.
 *
 * It is important to note that this event takes a while to complete, thus the thread blocks for up to 20
 * seconds.
 * @author Rudolf
 *
 */

public class StartSessionExample {

	public static final ObjectMapper MAPPER;

	private final static Client CLIENT;

	static {

		MAPPER = new ObjectMapper();

		CLIENT = ClientBuilder.newClient();
		CLIENT.register(HttpAuthenticationFeature.basicBuilder().build());

	}

	public static void main(String[] args) {
		try {
			WebTarget target = CLIENT.target(ExampleSettings.SERVER
					+ "api/services/event/IOServicesEventType/START_NEW_SESSION?f=JSON");
			Builder builder = target.request(MediaType.APPLICATION_JSON_TYPE);

			builder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_USERNAME,
					ExampleSettings.USERNAME);
			builder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_PASSWORD,
					ExampleSettings.PASSWORD);

			Object[] eventParams = new Object[] { "SINGLE_PLAYER", ExampleGame.GAME, ExampleGame.LANGUAGE,
					null, null };
			String jsonParams = MAPPER.writeValueAsString(eventParams);
			Response response = builder.post(Entity.json(jsonParams));

			String receivedString = response.readEntity(String.class);

			System.out.println(receivedString);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
