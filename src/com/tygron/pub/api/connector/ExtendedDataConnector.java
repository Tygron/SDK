package com.tygron.pub.api.connector;

import java.rmi.UnexpectedException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tygron.pub.api.connector.enums.GameMode;
import com.tygron.pub.logger.Log;
import com.tygron.pub.utils.JsonUtils;

public class ExtendedDataConnector extends DataConnector {

	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class JoinSessionObject {
		private String clientToken;
		private String serverToken;
		private SessionObject session;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class SessionObject {
		private Integer id;
	}

	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public static final String NULL = "null";

	public static final String NEW_SESSION_EVENT = "IOServicesEventType/START_NEW_SESSION";
	public static final String JOIN_SESSION_EVENT = "IOServicesEventType/JOIN_SESSION";

	public static final String STAKEHOLDER_SELECT_EVENT = "PlayerEventType/STAKEHOLDER_SELECT";
	public static final String SETTINGS_ALLOW_GAME_INTERACTION_EVENT = "SettingsLogicEventType/SETTINGS_ALLOW_GAME_INTERACTION";

	public static final String BUILDING_PLAN_CONSTRUCTION_EVENT = "PlayerEventType/BUILDING_PLAN_CONSTRUCTION";

	public static final String CLOSE_SESSION_EVENT = "IOServicesEventType/CLOSE_SESSION";

	private int clientID = -1;

	private boolean ignoreChecks = false;

	/**
	 * Pause or unpause the game.
	 * @param allowGameInteraction Whether players are allowed to interact with the game
	 */
	public void allowGameInteraction(boolean allowInteraction) {
		sendDataToServerSession(SETTINGS_ALLOW_GAME_INTERACTION_EVENT, (allowInteraction ? TRUE : FALSE));
	}

	/**
	 * Close the client's session with the server.
	 * @return True if the session on the server has closed as well. False if the session has remained open.
	 */
	public boolean closeConnectedSession() throws IllegalStateException {
		if (!ignoreChecks) {
			if (getServerSlot() == null) {
				throw new IllegalStateException("No serverslot set.");
			}
			if (getClientToken() == null) {
				throw new IllegalStateException("No client token set.");
			}
		}

		DataPackage data = sendDataToServer(CLOSE_SESSION_EVENT, getServerSlot(), getClientToken(), TRUE);

		return data.getContent().equals("true");
	}

	public int getClientID() {
		return this.clientID;
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
	 * Plan the construction of a building.
	 * @param stakeholderID The enacting stakeholder
	 * @param functionID The id of the function for the building (the building type)
	 * @param floors The amount of floors this building should have
	 * @param location A multipolygon String
	 */
	public void planBuilding(int stakeholderID, int functionID, int floors, String location) {
		sendDataToServerSession(BUILDING_PLAN_CONSTRUCTION_EVENT, Integer.toString(stakeholderID),
				Integer.toString(functionID), Integer.toString(floors), location);
	}

	/**
	 * Select a stakeholder for use during the session
	 * @param stakeholderID
	 * @return True when the stakeholder was selected successfully. False when the stakeholder was already
	 *         selected by another client
	 * @throws IllegalStateException If this DataConnector is missing credentials for interacting with the
	 *             server, this exception is thrown.
	 * @throws IllegalArgumentException If the response from the server indicates either the client ID or the
	 *             stakeholder ID is invalid, this exception is thrown.
	 */

	public boolean selectStakeholder(int stakeholderID) throws IllegalStateException,
	IllegalArgumentException {
		if (!ignoreChecks) {
			if (getServerSlot() == null) {
				throw new IllegalStateException("No serverslot set.");
			}
			if (getClientToken() == null) {
				throw new IllegalStateException("No client token set.");
			}
			if (getClientID() == -1) {
				throw new IllegalStateException("No client ID set.");
			}
		}

		DataPackage data = sendDataToServerSession(STAKEHOLDER_SELECT_EVENT, Integer.toString(stakeholderID),
				Integer.toString(getClientID()));

		switch (data.getContent()) {
			case TRUE:
				return true;
			case FALSE:
				return false;
			case NULL:
				throw new IllegalArgumentException("No stakeholder with ID: " + stakeholderID);
			default:
				throw new IllegalArgumentException("Unexpected response (probably non-existant client ID: "
						+ getClientID());
		}
	}

	public void setClientID(int clientID) {
		this.clientID = clientID;
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
	 * @return True when the session is started successfully. Failure conditions cause exceptions.
	 * @throws NullPointerException When a required argument to this method is null, this exception is
	 *             immediately thrown.
	 * @throws IllegalArgumentException When the gameMode or clientType are invalid, this exception is thrown.
	 * @throws UnexpectedException When the server responds in an unexpected fashion, this exception is
	 *             immediately thrown.
	 */
	public boolean startSessionAndConnect(final String serverAddress, final String username,
			final String password, final String gameName, final String language, final String gameMode,
			final String clientType, final String clientAddress, final String clientName)
					throws NullPointerException, IllegalArgumentException, UnexpectedException {

		if (!ignoreChecks) {
			if (serverAddress == null && (getServerAddress() == null)) {
				throw new NullPointerException("Serveraddress is null, but is required");
			}
			if (username == null && !hasCredentials()) {
				throw new NullPointerException("Username is null, but is required");
			}
			if (password == null && !hasCredentials()) {
				throw new NullPointerException("Password is null, but is required");
			}

			if (gameName == null) {
				throw new NullPointerException("Game name is null, but is required");
			}

			if (clientAddress == null) {
				throw new NullPointerException("Client address is null, but is required");
			}
			if (clientName == null) {
				throw new NullPointerException("Client name is null, but is required");
			}

			if (GameMode.valueOf(gameMode) == null) {
				throw new IllegalArgumentException("Game mode not recognized");
			}

			if (!GameMode.valueOf(gameMode).isValidClientType(clientType)) {
				throw new IllegalArgumentException("Client type invalid");
			}
		}

		if (!(username == null && password == null)) {
			setUsernameAndPassword(username, password);
		}

		if (serverAddress != null) {
			setServerAddress(serverAddress);
		}

		DataPackage data = sendDataToServer(NEW_SESSION_EVENT, gameMode, gameName, language, null, null);
		String slot = data.getContent();

		try {
			setServerSlot(slot);
		} catch (IllegalArgumentException e) {
			throw new UnexpectedException("Server slot response was unexpected: " + slot, e);
		}

		data = sendDataToServer(JOIN_SESSION_EVENT, slot, clientType, clientAddress, clientName, null);

		JoinSessionObject joinObject = JsonUtils.mapJsonToType(data.getContent(), JoinSessionObject.class);

		setServerToken(joinObject.serverToken);
		setClientToken(joinObject.clientToken);
		setClientID(joinObject.session.id);

		return true;
	}
}
