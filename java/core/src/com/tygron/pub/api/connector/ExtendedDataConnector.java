package com.tygron.pub.api.connector;

import java.rmi.UnexpectedException;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tygron.pub.api.enums.ClientType;
import com.tygron.pub.api.enums.GameMode;
import com.tygron.pub.api.enums.MapLink;
import com.tygron.pub.api.enums.events.ServerEvent;
import com.tygron.pub.api.enums.events.SessionEvent;
import com.tygron.pub.logger.Log;
import com.tygron.pub.utils.DataUtils;
import com.tygron.pub.utils.JsonUtils;
import com.tygron.pub.utils.StringUtils;
import com.tygron.pub.utils.ValueUtils;

public class ExtendedDataConnector extends DataConnector {

	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class ClientObject {
		// private Integer id;
		private String clientToken;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class CreateProjectObject {
		private String FILENAME;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class JoinSessionObject {
		private String serverToken;
		private ClientObject client;
	}

	private final static String URL_SEGMENT_SERVER_TOKEN_PARAMETER = "token=";

	private boolean ignoreChecks = false;

	/**
	 * Create a new ExtendedDataConnector, without any preset server or authentication information.
	 */
	public ExtendedDataConnector() {
	}

	/**
	 * Create a new ExtendedDataConnector based on an existing DataConnector. Authentication and server
	 * details copied to the new instance.
	 * @param dataConnector
	 */
	public ExtendedDataConnector(DataConnector dataConnector) {
		duplicateSettings(dataConnector);
	}

	/**
	 * Pause or unpause the game.
	 * @param allowGameInteraction Whether players are allowed to interact with the game
	 */
	public void allowGameInteraction(boolean allowInteraction) {
		parameterCheckDetails(true, true, false);
		sendDataToServerSession(SessionEvent.SETTINGS_ALLOW_GAME_INTERACTION.url(),
				(allowInteraction ? StringUtils.TRUE : StringUtils.FALSE));
	}

	/**
	 * Close the client's session with the server. If it's the last client connected to a session, it will
	 * attempt to close the session as well. This will succeed if the client is allowed to do so given the
	 * game mode.<br>
	 * For example, a multiplayer session can only be closed by a connected admin. A singleplayer session can
	 * (also) be closed by a connected viewer.
	 * @return True if the session on the server has closed as well. False if the session has remained open.
	 */
	public boolean closeConnectedSession() {
		parameterCheckDetails(true, false, true);

		DataPackage data = sendDataToServer(ServerEvent.CLOSE_SESSION.url(), getServerSlot(),
				getClientToken(), StringUtils.FALSE);
		return data.isContentTrue();
	}

	/**
	 * Start a session on the server, and join it.
	 * @param serverAddress The address for the server.
	 * @param username The username to use to authenticate.
	 * @param password The password to use to authenticate.
	 * @param gameName The name of the project to create.
	 * @param language The language in which to start the project.
	 * @return The name of the newly created project (which may differ from the provided gameName). Failure
	 *         conditions cause exceptions.
	 * @throws UnexpectedException When the server responds in an unexpected fashion, this exception is
	 *             immediately thrown.
	 */
	public String createGame(final String serverAddress, final String username, final String password,
			final String gameName, final String language) throws NullPointerException, UnexpectedException {
		parameterCheckCredentials(true, serverAddress, true, username, password, gameName);

		if (!(username == null && password == null)) {
			setUsernameAndPassword(username, password);
		}

		if (serverAddress != null) {
			setServerAddress(serverAddress);
		}

		String returnableGameName = null;

		DataPackage data = sendDataToServer(ServerEvent.CREATE_PROJECT.url(), gameName, language);

		try {
			CreateProjectObject gameData = JsonUtils.mapJsonToType(data.getContent(),
					CreateProjectObject.class);
			returnableGameName = gameData.FILENAME;
		} catch (Exception e) {
			throw new UnexpectedException("Failed to retrieve the game name of the created project", e);
		}

		return returnableGameName;
	}

	/**
	 * Prompt the server to begin loading geographical data into the game. This function may take up to
	 * several minutes, depending on the size of the map and availability of services. This function will
	 * attempt to set the map size, and then begin map generation. If the map size was already set, the
	 * original map size is NOT overridden. Coordinates are defined by the EPSG:3857 format.<br>
	 * Note that using this function when the project is not loaded in the editor, or performing other tasks
	 * in the same session may cause unpredictable behavior.
	 * @param mapSize Desired width and height of the map in meters.
	 * @param locationX X Coordinate according to the EPSG:3857 format.
	 * @param locationY Y Coordinate according to the EPSG:3857 format.
	 * @return The amount of failed processes during generation. 0 is success. More then 0 means some
	 *         datasources failed to load.
	 * @throws IllegalArgumentException If the map size is either 0 or smaller, or too large.
	 */
	public int generateMap(int mapSize, double locationX, double locationY) throws IllegalArgumentException {
		parameterCheckDetails(true, true, true);

		generateMapSize(mapSize);
		Log.verbose("Beginning map generation process: " + locationX + " by " + locationY);
		DataPackage data = sendDataToServerSession(SessionEvent.GENERATE_GAME.url(),
				Double.toString(locationX), Double.toString(locationY));

		int barsTotal;
		int barsComplete;
		int barsFailed;
		int barsCompleteSoFar = 0;
		do {
			barsTotal = 0;
			barsComplete = 0;
			barsFailed = 0;
			data = getDataFromServerSession(MapLink.PROGRESS);

			Map<Integer, Map<?, ?>> progressMap;
			try {
				progressMap = DataUtils.dataListToMap((List<Map<String, Map<?, ?>>>) (JsonUtils
						.mapJsonToList(data.getContent())));
			} catch (ClassCastException e) {
				throw new ClassCastException(
						"Failed to properly parse progress for the geographical generation process.");
			}
			barsTotal = progressMap.size();
			for (Map<?, ?> progressBar : progressMap.values()) {
				if (!(progressBar instanceof Map)) {
				}
				if (progressBar.get("progress").equals(1.0)) {
					barsComplete++;
				}
				if (progressBar.get("failed").equals(StringUtils.TRUE)) {
					barsFailed++;
				}
			}
			if (barsCompleteSoFar < barsComplete) {
				Log.verbose("Map generation progress complete: " + barsComplete + "/" + barsTotal);
				barsCompleteSoFar = barsComplete;
			}
		} while (barsTotal > barsComplete);

		return barsFailed;
	}

	/**
	 * Set the size of the map.
	 * @param mapSize Desired width and height of the map in meters.
	 * @throws IllegalArgumentException If the map size is either 0 or smaller, or too large.
	 * @throws IllegalStateException If the map is already assigned a size.
	 */
	public void generateMapSize(int mapSize) throws IllegalArgumentException, IllegalStateException {
		parameterCheckDetails(true, true, true);

		if (mapSize < ValueUtils.MIN_MAP_SIZE || mapSize > ValueUtils.MAX_MAP_SIZE) {
			throw new IllegalArgumentException("Map size must be between " + ValueUtils.MIN_MAP_SIZE
					+ " and " + ValueUtils.MAX_MAP_SIZE);
		}

		if (!getIgnoreChecks()) {
			DataPackage data = getDataFromServerSession(MapLink.SETTINGS.itemUrl(ValueUtils.SETTING_MAP_SIZE));
			Map<String, String> mapSizeSetting = (Map<String, String>) JsonUtils.mapJsonToMap(data
					.getContent());
			if (!mapSizeSetting.get("value").equals("0")) {
				throw new IllegalStateException("Map has already been sized to: "
						+ mapSizeSetting.get("value"));
			}
		}
		DataPackage data = sendDataToServerSession(SessionEvent.SET_MAP_SIZE.url(), Integer.toString(mapSize));
	}

	/**
	 * A convenience method which returns the URL for the game this DataConnector is currently connected to.
	 * @return The URL this DataConnector is connected to.
	 */
	public String getBrowserURL() {
		if (getServerToken() == null) {
			throw new IllegalStateException("Server token required, but not set");
		}
		String fullURL = createFullURL("", true, true, false, false, false);
		fullURL += (fullURL.contains("?") ? "&" : "?") + URL_SEGMENT_SERVER_TOKEN_PARAMETER
				+ getServerToken();
		return fullURL;
	}

	/**
	 * Indicates whether checks for missing credentials and such should be performed when performing certain
	 * complex operations.
	 * @return True when checks are being skipped. False when checks will be performed.
	 */
	public boolean getIgnoreChecks() {
		return ignoreChecks;
	}

	/**
	 * Used to check whether this ExtendedDataConnector is ready to be used as a player.
	 * @return
	 */
	public boolean isPlayerReady() {
		try {
			parameterCheckDetails(true, true, true);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Join a session on the server.
	 * @param serverAddress The address for the server.
	 * @param username The username to use to authenticate.
	 * @param password The password to use to authenticate.
	 * @param serverSlot The serverslot currently containing the game you wish to join.
	 * @param clientType The type of client as which to connect to the game. This should match a ClientType
	 *            enum's String representation.
	 * @param clientAddress The IP-address of your client.
	 * @param clientName The name of your client
	 *
	 * @return The name of the newly created project (which may differ from the provided gameName). Failure
	 *         conditions cause exceptions.
	 * @throws UnexpectedException When the server responds in an unexpected fashion, this exception is
	 *             immediately thrown.
	 */
	public boolean joinSession(final String serverAddress, final String username, final String password,
			final String clientType, final String serverSlot, final String clientAddress,
			final String clientName) throws UnexpectedException {
		parameterCheckCredentials(true, serverAddress, true, username, password, serverSlot, clientAddress,
				clientName);

		if (!(username == null && password == null)) {
			setUsernameAndPassword(username, password);
		}

		if (serverAddress != null) {
			setServerAddress(serverAddress);
		}

		try {
			setServerSlot(serverSlot);
		} catch (IllegalArgumentException e) {
			throw new UnexpectedException("Server slot response was unexpected or invalid: " + serverSlot, e);
		}
		if (getServerSlot() == null) {
			return false;
		}

		DataPackage data = sendDataToServer(ServerEvent.JOIN_SESSION.url(), serverSlot, clientType,
				clientAddress, clientName, null);

		JoinSessionObject joinObject = JsonUtils.mapJsonToType(data.getContent(), JoinSessionObject.class);

		setServerToken(joinObject.serverToken);
		setClientToken(joinObject.client.clientToken);

		return true;
	}

	private void parameterCheck(final String... otherParams) {
		if (!ignoreChecks) {
			for (String s : otherParams) {
				if (s == null) {
					throw new NullPointerException("Required value missing");
				}
			}
		}
	}

	private void parameterCheckCredentials(//
			final boolean serverAddressRequired, final String serverAddress, //
			final boolean usernameAndPasswordRequired, final String username, final String password,//
			final String... otherParams) {
		if (!ignoreChecks) {
			if (serverAddressRequired) {
				if (serverAddress == null && getServerAddress() == null) {
					throw new IllegalStateException("ServerAddress is null, but is required");
				}
			}
			if (usernameAndPasswordRequired) {
				if (username == null && !hasCredentials()) {
					throw new IllegalStateException("Username is null, but is required");
				}
				if (password == null && !hasCredentials()) {
					throw new IllegalStateException("Password is null, but is required");
				}
			}
			parameterCheck(otherParams);
		}
	}

	private void parameterCheckDetails(final boolean serverSlotRequired, final boolean serverTokenRequired,
			final boolean clientTokenRequired, final String... otherParams) {
		if (!ignoreChecks) {
			if (serverSlotRequired) {
				if (getServerSlot() == null) {
					throw new IllegalStateException("No serverslot set, but is required");
				}
			}
			if (serverTokenRequired) {
				if (getServerToken() == null) {
					throw new IllegalStateException("ServerToken is null, but is required");
				}
			}
			if (clientTokenRequired) {
				if (getClientToken() == null) {
					throw new IllegalStateException("ClientToken is null, but is required");
				}
			}
			parameterCheck(otherParams);
		}
	}

	private void parameterCheckGameAndClient(final boolean gameModeRequired, final String gameMode,
			final boolean clientTypeRequired, final String clientType, final boolean typeMustMatchMode) {
		if (!ignoreChecks) {
			if (gameModeRequired) {
				if (GameMode.valueOf(gameMode) == null) {
					throw new IllegalArgumentException("Game mode not recognized");
				}
			}

			if (clientTypeRequired) {
				if (ClientType.valueOf(clientType) == null) {
					throw new IllegalArgumentException("Client type not recognized");
				}
			}

			if (typeMustMatchMode) {
				if (!GameMode.valueOf(gameMode).isValidClientType(clientType)) {
					throw new IllegalArgumentException("Client type invalid");
				}
			}
		}
	}

	/**
	 * Set whether to check for missing credentials and such.
	 * @param ignoreChecks Whether to ignore checks before performing certain complex operations.
	 */
	public void setIgnoreChecks(boolean ignoreChecks) {
		this.ignoreChecks = ignoreChecks;
		Log.verbose("Checks ignoring set to: " + ignoreChecks);
	}

	/**
	 * Start a session on the server, and connect to it.
	 * @param serverAddress The address for the server.
	 * @param username The username to use to authenticate.
	 * @param password The password to use to authenticate.
	 * @param gameName The name of the project to start
	 * @param language The language in which to start the project (leave null for default)
	 * @param gameMode The mode in which the project should be started. This should match a GameMode enum's
	 *            String representation.
	 * @param clientType The type of client as which to connect to the game. This should match a ClientType
	 *            enum's String representation.
	 * @param clientAddress The IP-address of your client.
	 * @param clientName The name of your client
	 * @return True when the session is started successfully. False if the session failed to start.
	 * @throws NullPointerException When a required argument to this method is null, this exception is
	 *             immediately thrown.
	 * @throws UnexpectedException When the server responds in an unexpected fashion, this exception is
	 *             immediately thrown.
	 */
	public boolean startSessionAndJoin(final String serverAddress, final String username,
			final String password, final String gameName, final String language, final String gameMode,
			final String clientType, final String clientAddress, final String clientName)
			throws NullPointerException, UnexpectedException {

		parameterCheckCredentials(true, serverAddress, true, username, password, clientAddress, clientName);
		parameterCheckGameAndClient(true, gameMode, true, clientType, true);

		if (!(username == null && password == null)) {
			setUsernameAndPassword(username, password);
		}

		if (serverAddress != null) {
			setServerAddress(serverAddress);
		}

		DataPackage data = sendDataToServer(ServerEvent.NEW_SESSION.url(), gameMode, gameName, language,
				null, null);
		String slot = data.getContent();

		return joinSession(null, null, null, clientType, slot, clientAddress, clientName);
	}
}
