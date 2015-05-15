package com.tygron.pub.examples.standalone.apiserver;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 *
 * @author Maxim
 *
 */

@Path("/")
public class ExampleServerConfig1 {

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ApiPost {

		private String slotAddress;

		private String token;

		private Integer indicatorID;

		public ApiPost() {

		}
	}

	public static final ObjectMapper MAPPER;

	static {

		MAPPER = new ObjectMapper();
		MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
		MAPPER.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
		MAPPER.enable(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS);
		MAPPER.enable(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN);

		MAPPER.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		MAPPER.setVisibility(PropertyAccessor.GETTER, Visibility.NONE);
		MAPPER.setVisibility(PropertyAccessor.IS_GETTER, Visibility.NONE);
		MAPPER.setVisibility(PropertyAccessor.SETTER, Visibility.NONE);

	}

	private final static Client CLIENT = ClientBuilder.newClient();

	/**
	 * This is the method called when the API Indicator has to be updated.
	 * @param json The Json provided by the server, containing the "slotAddress", "token" and "indicatorID".
	 */
	@POST
	@Path("run")
	@Produces(MediaType.APPLICATION_JSON)
	public void runModel(final String json) {
		/**
		 * Verify in the console that this function has been called.
		 */
		System.out.println("RunModel called");

		/**
		 * Calculation can take a while, so we perform it on a separate thread, keeping the server available
		 * for additional calls.
		 */
		new Thread() {
			@Override
			public void run() {
				System.out.println("Running Model...");
				try {
					/**
					 * Read JSON post contents. This provides us with the server that's making the call, the
					 * token to interact with that server, and the ID of the indicator which should be
					 * updated.
					 */
					ApiPost post = MAPPER.readValue(json, ApiPost.class);
					System.out.println("Received model run request: " + json);

					/**
					 * GET amount of Buildings from the Tygron Engine, using the provided token and server
					 * slot ID.
					 */
					WebTarget target = CLIENT.target(post.slotAddress + "lists/buildings/size/?f=JSON");
					Builder builder = target.request(MediaType.APPLICATION_JSON_TYPE);
					builder.header("serverToken", post.token);
					Response response = builder.get();
					String jsonResponse = response.readEntity(String.class);
					double size = Integer.valueOf(jsonResponse);
					System.out.println("GET amount of buildings from Tygron Engine: " + size);

					/**
					 * We set a target for 1000 buildings. (Note that for this example, this target is
					 * "hard-coded" serverside. Any target set in the project when creating the indicator is
					 * not retrieved and implicitly ignored. If you wish to honor the target set in the Tygron
					 * Engine, retrieve it and use it in your calculation.)
					 */
					int indicatorTarget = 1000;

					String explaination = "We have " + size + " buildings, we need: " + indicatorTarget;
					/**
					 * Make a simple calculation. Scoring goes from 0.0 (0%) to 1.0 (100%). Any value lower or
					 * higher is ignored by the Tygron Engine. For clarity: If the value is outside of the
					 * range 0.0 (inclusive) to 1.0 (inclusive), the value is not clamped, but ignored, and
					 * the API indicator will not be updated.
					 *
					 */
					double value = Math.max(0, Math.min(1, size / indicatorTarget));
					Object[] eventParams = new Object[] { post.indicatorID, explaination, value };

					/**
					 * POST the result back to the Tygron Engine. The slot address is something along the
					 * lines of "https://www.tygronengine.com/api/slots/0/", as we received from the server
					 * when it made its request for an update.
					 */
					target = CLIENT.target(post.slotAddress + "event/LogicEventType/API_INDICATOR_SET_VALUE");
					builder = target.request(MediaType.APPLICATION_JSON_TYPE);
					builder.header("serverToken", post.token);
					String jsonParams = MAPPER.writeValueAsString(eventParams); // Let the mapper turn the
																				// parameters into a string
					builder.post(Entity.json(jsonParams));
					System.out.println("POST new indicator result: " + jsonParams + "\n");

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	@GET
	// @Path("run")
	@Produces(MediaType.TEXT_HTML)
	public String testServerLives() {
		return "The server lives! You should set the server url in the engine to this URL, appended by \"run\".<br> (For example, if the request you have made is \"http://myserver.com\", you should set it to \"http://myserver.com/run\")";
	}
}
