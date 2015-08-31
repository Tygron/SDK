package com.tygron.pub.api.connector;

import java.net.SocketException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import com.tygron.pub.api.enums.EventType.ServerEventType;
import com.tygron.pub.api.enums.EventType.SessionEventType;
import com.tygron.pub.api.enums.MapLink;
import com.tygron.pub.exceptions.AuthenticationException;
import com.tygron.pub.exceptions.IncompleteResponseException;
import com.tygron.pub.exceptions.NoSuchServerException;
import com.tygron.pub.exceptions.PageNotFoundException;
import com.tygron.pub.logger.Log;
import com.tygron.pub.utils.JsonUtils;
import com.tygron.pub.utils.StringUtils;

public class DataConnector {

	public enum RequestType {
			GET,
			POST;
	}

	private final static Client CLIENT;
	private final static MediaType DEFAULT_MEDIA_TYPE = MediaType.WILDCARD_TYPE;

	static {
		CLIENT = ClientBuilder.newClient();
		CLIENT.register(HttpAuthenticationFeature.basicBuilder().build());
	}

	private String username = null;
	private String password = null;
	private String serverToken = null;
	private String clientToken = null;

	private String serverSlot = null;
	private String serverAddress = null;

	private int autoRetryOnIncompleteResponse = 3;

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
		duplicateSettings(dataConnector);
	}

	/**
	 * Create a new DataConnector, with a preset server address.
	 * @param serverAddress
	 */
	public DataConnector(String serverAddress) {
		this.setServerAddress(serverAddress);
	}

	private String convertToParameters(Object... params) {
		if (params != null && params.length == 1 && params[0] != null && params[0] instanceof Map) {
			return JsonUtils.mapObjectToJson(params[0]);
		}
		return JsonUtils.mapObjectToJson(params);
	}

	protected String createFullURL(final String url, final boolean addServer, final boolean addSlot,
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
			prefix += StringUtils.URL_SEGMENT_SERVERSLOT + serverSlot + StringUtils.URL_DELIMITER;
		}
		if (addList) {
			prefix += StringUtils.URL_SEGMENT_LISTS;
		}
		if (addServices) {
			prefix += StringUtils.URL_SEGMENT_SERVICES;
		}
		if (addEvent) {
			prefix += StringUtils.URL_SEGMENT_EVENT;
		}

		if (!url.contains(StringUtils.URL_SEGMENT_JSON_QUERY_PARAMETER)) {
			postfix = (url.contains("?") ? "&" : "?") + StringUtils.URL_SEGMENT_JSON_QUERY_PARAMETER;
		}

		return prefix + url + postfix;
	}

	/**
	 * Duplicate the settings from another DataConnector into this DataConnector.
	 * @param dataConnector
	 */
	protected void duplicateSettings(DataConnector dataConnector) {
		this.username = dataConnector.username;
		this.password = dataConnector.password;
		this.serverToken = dataConnector.serverToken;
		this.clientToken = dataConnector.clientToken;
		this.serverSlot = dataConnector.serverSlot;
		this.serverAddress = dataConnector.serverAddress;
	}

	public int getAutoRetryAttempts() {
		return this.autoRetryOnIncompleteResponse;
	}

	public String getClientToken() {
		return this.clientToken;
	}

	/**
	 * Request data from [url]
	 */
	public DataPackage getData(String url) {
		return makeRequestToURL(url, RequestType.GET);
	}

	/**
	 * Request data from https://www.tygronengine.com/api/services/[url]
	 */
	public DataPackage getDataFromServer(String url) {
		String fullURL = createFullURL(url, true, false, false, true, false);
		return makeRequestToURL(fullURL, RequestType.GET);
	}

	/**
	 * Request data from https://www.tygronengine.com/api/slots/X/lists/[url]
	 */
	public DataPackage getDataFromServerSession(MapLink mapLink) {
		return getDataFromServerSession(mapLink.url());
	}

	/**
	 * Request data from https://www.tygronengine.com/api/slots/X/lists/[url]
	 */
	public DataPackage getDataFromServerSession(String url) {
		String fullURL = createFullURL(url, true, true, true, false, false);
		return makeRequestToURL(fullURL, RequestType.GET);
	}

	/**
	 * Request data from https://www.tygronengine.com/api/slots/X/location/
	 */
	public DataPackage getDataFromServerSessionLocation() {
		String fullURL = createFullURL(StringUtils.URL_SEGMENT_LOCATION, true, true, false, false, false);
		return makeRequestToURL(fullURL, RequestType.GET);
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

	public String getUsername() {
		return this.username;
	}

	public boolean hasCredentials() {
		return (this.username != null) && (this.password != null);
	}

	private Response makeRequest(String url, RequestType requestType, Object[] params)
			throws IllegalArgumentException {

		Log.verbose("Making " + requestType.toString() + " request to: " + url);

		WebTarget target = CLIENT.target(url);
		Response response;

		Builder builder = target.request(DEFAULT_MEDIA_TYPE);
		builder = setCredentials(builder);

		try {
			switch (requestType) {
				case GET:
					response = builder.get();
					break;
				case POST:
				default:
					String jsonParams = convertToParameters(params);
					response = builder.post(Entity.json(jsonParams));
					break;
			}
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Unable to parse parameters for request into Json",
					e.getCause() != null ? e.getCause() : e);
		} catch (ProcessingException e) {
			if (e.getCause() instanceof SocketException) {
				throw new IncompleteResponseException(e.getMessage(), e.getCause());
			} else if (e.getCause() instanceof IllegalStateException) {
				throw new NoSuchServerException();
			} else {
				throw e;
			}
		} catch (Exception e) {
			throw e;
		}

		return response;
	}

	public DataPackage makeRequestToURL(String url, RequestType requestType, Object... params) {
		String receivedString = StringUtils.EMPTY;

		long requestTime = StringUtils.NOTHING;
		int statusCode = StringUtils.NOTHING;

		Response response = null;

		for (int i = autoRetryOnIncompleteResponse; i >= 0; i++) {
			try {
				requestTime = System.currentTimeMillis();
				response = makeRequest(url, requestType, params);
				requestTime = System.currentTimeMillis() - requestTime;
				break;
			} catch (IncompleteResponseException e) {
				if (i == 0) {
					throw e;
				}
			}
		}

		statusCode = response.getStatus();
		receivedString = response.readEntity(String.class);
		Log.verbose("Received: " + receivedString);

		if (receivedString == null || StringUtils.EMPTY.equals(receivedString)) {
			Log.verbose("No contents from request to: " + url);
		}

		if (statusCode == 401) {
			throw new AuthenticationException();
		}

		if (statusCode == 404) {
			throw new PageNotFoundException();
		}

		return new DataPackage(receivedString, requestTime, statusCode);
	}

	/**
	 * Send POST request to [url], without parameters
	 */
	public DataPackage sendData(String url) {
		return makeRequestToURL(url, RequestType.POST, (String) null);
	}

	/**
	 * Send POST request to [url]
	 */
	public DataPackage sendData(String url, Object... params) {
		return makeRequestToURL(url, RequestType.POST, params);
	}

	/**
	 * Send POST request to https://www.tygronengine.com/api/services/event/[url]
	 */
	public DataPackage sendDataToServer(ServerEventType event, Object... params) {
		return sendDataToServer(event.url(), params);
	}

	/**
	 * Send POST request to https://www.tygronengine.com/api/services/event/[url]
	 */
	public DataPackage sendDataToServer(String url, Object... params) {
		String fullURL = createFullURL(url, true, false, false, true, true);
		return makeRequestToURL(fullURL, RequestType.POST, params);
	}

	/**
	 * Send POST request to https://www.tygronengine.com/api/slots/X/event/[url]
	 */
	public DataPackage sendDataToServerSession(SessionEventType event, Object... params) {
		return sendDataToServerSession(event.url(), params);
	}

	/**
	 * Send POST request to https://www.tygronengine.com/api/slots/X/event/[url]
	 */
	public DataPackage sendDataToServerSession(String url, Object... params) {
		String fullURL = createFullURL(url, true, true, false, false, true);
		return makeRequestToURL(fullURL, RequestType.POST, params);
	}

	public DataPackage sendUpdateRequestToServerSession(Map<String, Integer> params) {
		String fullURL = createFullURL(StringUtils.URL_SEGMENT_UPDATE, true, true, false, false, false);
		return makeRequestToURL(fullURL, RequestType.POST, params);
	}

	public void setAutoRetryAttempts(int attempts) {
		this.autoRetryOnIncompleteResponse = Math.max(0, attempts);
	}

	public void setClientToken(final String clientToken) {
		this.clientToken = clientToken;
	}

	private Builder setCredentials(Builder builder) {
		if (this.username != null && this.password != null) {
			builder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_USERNAME, username);
			builder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_PASSWORD, password);
		} else {
			Log.verbose("Username or password are not set");
			builder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_USERNAME, StringUtils.SPACE);
			builder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_PASSWORD, StringUtils.SPACE);
		}

		if (this.serverToken != null) {
			builder.header("serverToken", this.serverToken);
		}
		if (this.clientToken != null) {
			builder.header("clientToken", this.clientToken);
		}

		return builder;
	}

	public void setServerAddress(final String serverAddress) {
		this.serverAddress = serverAddress;

		if (serverAddress != null) {
			if (!this.serverAddress.endsWith(StringUtils.URL_DELIMITER)) {
				this.serverAddress += StringUtils.URL_DELIMITER;
			}
			if (!this.serverAddress.endsWith(StringUtils.URL_SEGMENT_API)) {
				this.serverAddress += StringUtils.URL_SEGMENT_API;
			}
		}
	}

	public void setServerSlot(final String serverSlot) throws IllegalArgumentException {
		try {
			Integer slot = Integer.parseInt(serverSlot);
			if (slot < 0) {
				this.serverSlot = null;
			} else {
				this.serverSlot = slot.toString();
			}
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Server slot must be a number", e);
		}
	}

	public void setServerToken(final String serverToken) {
		this.serverToken = serverToken;
	}

	public void setSlotAddress(String slotAddress) {
		// https://server2.tygron.com:3020/api/slots/0
		String serverAddress = StringUtils.EMPTY;
		String serverSlot = StringUtils.EMPTY;

		try {

			serverAddress = slotAddress.split("(" + StringUtils.URL_SEGMENT_API + ")")[0];

			Matcher matcher = Pattern.compile(
					StringUtils.URL_DELIMITER + "([0-9]+)" + StringUtils.URL_DELIMITER).matcher(slotAddress);
			matcher.find();
			serverSlot = matcher.group(1);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Failed to parse slot address into server address and slot number.", e);
		}
		if (StringUtils.isEmpty(serverAddress) || StringUtils.isEmpty(serverSlot)) {
			throw new IllegalArgumentException(
					"Provided slot address did not contain server address and slot number");
		}

		setServerAddress(serverAddress);
		setServerSlot(serverSlot);
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
