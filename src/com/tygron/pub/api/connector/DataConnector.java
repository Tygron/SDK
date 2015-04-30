package com.tygron.pub.api.connector;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import com.tygron.pub.api.connector.listeners.UpdateListenerInterface;
import com.tygron.pub.logger.Log;
import com.tygron.pub.utils.JsonUtils;
import com.tygron.pub.utils.StringUtils;

public class DataConnector {

	private final static Client CLIENT;

	static {
		CLIENT = ClientBuilder.newClient();
		CLIENT.register(HttpAuthenticationFeature.basicBuilder().build());
	}

	private final static String URL_SEGMENT_API = "api/";
	private final static String URL_SEGMENT_SERVERSLOT = "slots/";
	private final static String URL_SEGMENT_LISTS = "lists/";
	private final static String URL_SEGMENT_EVENT = "event/";
	private final static String URL_SEGMENT_SERVICES = "services/";

	private final static String URL_DELIMITER = "/";
	private final static String URL_SEGMENT_JSON_QUERY_PARAMETER = "f=JSON";

	private String username = null;
	private String password = null;
	private String serverToken = null;
	private String clientToken = null;

	private String serverSlot = null;
	private String serverAddress = null;

	private int version = -1;

	List<UpdateListenerInterface> listeners = new ArrayList<UpdateListenerInterface>();

	/**
	 * Create a new DataConnector, without any preset server or authentication information.
	 */
	public DataConnector() {
	}

	/**
	 * Create a new DataConnector based on an existing DataConnector. Authentication and server details copied
	 * to the new instance.
	 * @param dataConnector
	 */
	public DataConnector(DataConnector dataConnector) {
		this.username = dataConnector.username;
		this.password = dataConnector.password;
		this.serverToken = dataConnector.serverToken;
		this.clientToken = dataConnector.clientToken;
		this.serverSlot = dataConnector.serverSlot;
		this.serverAddress = dataConnector.serverAddress;
	}

	/**
	 * Create a new DataConnector, with a preset server address.
	 * @param serverAddress
	 */
	public DataConnector(String serverAddress) {
		this.setServerAddress(serverAddress);
	}

	/**
	 * Register a listener on this connector, which will be informed of updates when this Connector listens
	 * for updates and receives them.
	 * @param listener The Listener to add.
	 */
	public void addListener(UpdateListenerInterface listener) {
		if (!listeners.contains(listener) && listener != null) {
			listeners.add(listener);
		}
	}

	private String createFullURL(final String url, final boolean addServer, final boolean addSlot,
			final boolean addList, final boolean addServices, final boolean addEvent)
					throws IllegalStateException {
		String prefix = StringUtils.EMPTY;
		String postfix = StringUtils.EMPTY;

		if (addServer) {
			if (serverAddress == null) {
				throw new IllegalStateException("Server address required, but not set");
			}
			prefix += serverAddress;
		}

		if (addSlot) {
			if (serverSlot == null) {
				throw new IllegalStateException("Server slot required, but not set");
			}

			// if (serverToken == null) {
			// throw new IllegalStateException("Server token required, but not set");
			// }
			prefix += URL_SEGMENT_SERVERSLOT + serverSlot + URL_DELIMITER;
		}
		if (addList) {
			prefix += URL_SEGMENT_LISTS;
		}
		if (addServices) {
			prefix += URL_SEGMENT_SERVICES;
		}
		if (addEvent) {
			prefix += URL_SEGMENT_EVENT;
		}

		if (!url.contains(URL_SEGMENT_JSON_QUERY_PARAMETER)) {
			postfix = (url.contains("?") ? "&" : "?") + URL_SEGMENT_JSON_QUERY_PARAMETER;
		}

		return prefix + url + postfix;
	}

	public String getClientToken() {
		return this.clientToken;
	}

	/**
	 * Request data from [url]
	 */
	public DataPackage getData(String url) {
		return makeRequestToURL(url, true);
	}

	/**
	 * Request data from https://www.tygronengine.com/api/services/[url]
	 */
	public DataPackage getDataFromServer(String url) {
		String fullURL = createFullURL(url, true, false, false, true, false);
		return makeRequestToURL(fullURL, true);
	}

	/**
	 * Request data from https://www.tygronengine.com/api/slots/X/lists/[url]
	 */
	public DataPackage getDataFromServerSession(String url) {
		String fullURL = createFullURL(url, true, true, true, false, false);
		return makeRequestToURL(fullURL, true);
	}

	/**
	 * Get a list of all listeners currently registered on this Connector.
	 * @return A list of all listeners currently registered on this Connector.
	 */
	public List<UpdateListenerInterface> getListeners() {
		return new ArrayList<UpdateListenerInterface>(listeners);
	}

	public int getListeningVersion() {
		return this.version;
	}

	public String getServerAddress() {
		return this.serverAddress;
	}

	public String getServerSlot() {
		return this.serverSlot;
	}

	public String getServerToken() {
		return this.serverToken;
	}

	public boolean hasCredentials() {
		return (this.username != null) && (this.password != null);
	}

	public void listenForUpdate() {

	}

	public void listenForUpdate(int version) {
		setListeningVersion(version);
		listenForUpdate();
	}

	private Response makeRequest(String url, boolean getRequest, Object[] params)
			throws IllegalArgumentException {

		Log.verbose("Making " + (getRequest ? "GET" : "POST") + " request to: " + url);

		WebTarget target = CLIENT.target(url);
		Response response;

		Builder builder = target.request(MediaType.APPLICATION_JSON_TYPE);
		builder = setCredentials(builder);

		try {
			if (getRequest) {
				response = builder.get();
			} else {
				builder.header("f", "JSON");
				String jsonParams = JsonUtils.mapObjectToJson(params);
				response = builder.post(Entity.json(jsonParams));
			}
		} catch (IllegalArgumentException e) {
			if (e.getCause() != null) {
				throw new IllegalArgumentException("Unable to parse parameters for request into Json",
						e.getCause());
			} else {
				throw new IllegalArgumentException("Unable to parse parameters for request into Json", e);
			}
		}

		return response;
	}

	public DataPackage makeRequestToURL(String url, boolean getRequest, String... params) {
		String receivedString = StringUtils.EMPTY;

		long requestTime = -1;
		int statusCode = -1;

		requestTime = System.currentTimeMillis();
		Response response = makeRequest(url, getRequest, params);
		requestTime = System.currentTimeMillis() - requestTime;

		statusCode = response.getStatus();
		receivedString = response.readEntity(String.class);
		Log.verbose("Received: " + receivedString);

		if (receivedString == null || StringUtils.EMPTY.equals(receivedString)) {
			Log.verbose("No contents from request to: " + url);
		}

		return new DataPackage(receivedString, requestTime, statusCode);
	}

	/**
	 * Remove a registered listener from this Connector. The listener will no longer be informed of updates.
	 * @param listener The listener to remove from this Connector.
	 */
	public void removeListener(UpdateListenerInterface listener) {
		if (listeners.contains(listener)) {
			listeners.remove(listener);
		}
	}

	public void resetListeningVersion() {
		this.version = -1;
	}

	/**
	 * Send POST request to [url], without parameters
	 */
	public DataPackage sendData(String url) {
		return makeRequestToURL(url, false, (String[]) null);
	}

	/**
	 * Send POST request to [url]
	 */
	public DataPackage sendData(String url, String... params) {
		return makeRequestToURL(url, false, params);
	}

	/**
	 * Send POST request to https://www.tygronengine.com/api/services/event/[url]
	 */
	public DataPackage sendDataToServer(String url, String... params) {
		String fullURL = createFullURL(url, true, false, false, true, true);
		return makeRequestToURL(fullURL, false, params);
	}

	/**
	 * Send POST request to https://www.tygronengine.com/api/slots/X/event/[url]
	 */
	public DataPackage sendDataToServerSession(String url, String... params) {
		String fullURL = createFullURL(url, true, true, false, false, true);
		return makeRequestToURL(fullURL, false, params);
	}

	public void setClientToken(final String clientToken) {
		this.clientToken = clientToken;
	}

	private Builder setCredentials(Builder builder) {
		if (this.username != null && this.password != null) {
			builder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_USERNAME, username);
			builder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_PASSWORD, password);
		}

		if (this.serverToken != null) {
			builder.header("serverToken", this.serverToken);
		}
		if (this.clientToken != null) {
			builder.header("clientToken", this.clientToken);
		}

		return builder;
	}

	public void setListeningVersion(int version) {
		this.version = version;
	}

	public void setServerAddress(final String serverAddress) {
		this.serverAddress = serverAddress;

		if (serverAddress != null) {
			if (!this.serverAddress.endsWith(URL_DELIMITER)) {
				this.serverAddress += URL_DELIMITER;
			}
			if (!this.serverAddress.endsWith(URL_SEGMENT_API)) {
				this.serverAddress += URL_SEGMENT_API;
			}
		}
	}

	public void setServerSlot(final String serverSlot) throws IllegalArgumentException {
		try {
			Integer slot = Integer.parseInt(serverSlot);
			this.serverSlot = slot.toString();
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Server slot must be a number", e);
		}
	}

	public void setServerToken(final String serverToken) {
		this.serverToken = serverToken;
	}

	public void setUsernameAndPassword(final String username, final String password)
			throws IllegalArgumentException {
		if (username == null && password == null) {
			this.username = null;
			this.password = null;
		}

		if (!(username == null && password == null)) {
			if ((this.username == null && username == null) || (this.password == null && password == null)) {
				throw new IllegalArgumentException(
						"Both username and password are required, unless setting both to null.");
			}
			if (username != null) {
				this.username = username;
			}
			if (password != null) {
				this.password = password;
			}
		}
	}
}
