package com.tygron.pub.api.connector.modules;

import com.tygron.pub.api.connector.DataConnector;
import com.tygron.pub.api.connector.DataPackage;
import com.tygron.pub.api.data.item.Building;
import com.tygron.pub.api.data.item.Item;
import com.tygron.pub.api.data.item.Message;
import com.tygron.pub.api.data.item.Popup;
import com.tygron.pub.api.enums.MapLink;
import com.tygron.pub.api.enums.events.SessionEvent;
import com.tygron.pub.utils.JsonUtils;
import com.tygron.pub.utils.StringUtils;

public class PlayerModule {
	private DataConnector dataConnector = null;
	private int stakeholderID = StringUtils.NOTHING;

	/**
	 * Create a new PlayerModule, and immediately link it to a DataConnector.
	 */
	public PlayerModule(DataConnector dataConnector) {
		setDataConnector(dataConnector);
	}

	/**
	 * Retrieve a specific building.
	 * @param messageID The ID of the building.
	 * @return The Building.
	 */
	public Building buildingGetBuilding(int buildingID) {
		isPlayerReady();

		DataPackage data = dataConnector.getDataFromServerSession(MapLink.BUILDINGS.itemUrl(buildingID));
		if (data.getStatusCode() == 500) {
			return null;
		}
		Building building = JsonUtils.mapJsonToType(data.getContent(), Item.get(Building.class));

		return building;
	}

	/**
	 * Plan the construction of a building.
	 * @param functionID The id of the function for the building (the building type).
	 * @param floors The amount of floors this building should have.
	 * @param location A multipolygon String.
	 */
	public void buildingPlanConstruction(int functionID, int floors, String location) {
		isPlayerReady();

		sendPlayerEvent(SessionEvent.BUILDING_PLAN_CONSTRUCTION, Integer.toString(functionID),
				Integer.toString(floors), location);
	}

	/**
	 * Plan the construction of a building.
	 * @param building The building to demolish.
	 */
	public void buildingPlanDemolish(Building building) {
		isPlayerReady();

		sendPlayerEvent(SessionEvent.BUILDING_PLAN_DEMOLISH, Integer.toString(building.getID()));
	}

	/**
	 * Plan the construction of a building.
	 * @param buildingID The building to demolish.
	 */
	public void buildingPlanDemolish(int buildingID) {
		isPlayerReady();

		sendPlayerEvent(SessionEvent.BUILDING_PLAN_DEMOLISH, Integer.toString(buildingID));
	}

	/**
	 * Get the current StakeholderID.
	 * @return The player's stakeholderID.
	 */
	public int getStakeholderID() {
		return stakeholderID;
	}

	protected boolean isPlayerReady() {
		return isPlayerReady(true);
	}

	protected boolean isPlayerReady(boolean stakeholder) {
		if (dataConnector == null || dataConnector.getServerSlot() == null
				|| dataConnector.getServerToken() == null || dataConnector.getClientToken() == null
				|| (stakeholder && stakeholderID == StringUtils.NOTHING)) {
			throw new IllegalStateException("The registered DataConnector is not ready");
		}
		return true;
	}

	/**
	 * Answer a message with a specific answer.
	 * @param message The message to answer.
	 * @param answerID The ID of the answer to give to the message.
	 */
	public void messageAnswerMessage(int messageID, int answerID) {
		isPlayerReady();
		sendPlayerEvent(SessionEvent.MESSAGE_ANSWER, Integer.toString(messageID), Integer.toString(answerID));
	}

	/**
	 * Answer a message with a specific answer.
	 * @param message The message to answer.
	 * @param answerID The content of the answer to give to the message.
	 */
	public void messageAnswerMessage(Message message, String answer) {
		isPlayerReady();
		sendPlayerEvent(SessionEvent.MESSAGE_ANSWER, Integer.toString(message.getID()),
				Integer.toString(message.getAnswerID(answer)));
	}

	/**
	 * Retrieve a specific message.
	 * @param messageID The ID of the message.
	 * @return The Message.
	 */
	public Message messageGetMessage(int messageID) {
		isPlayerReady();

		DataPackage data = dataConnector.getDataFromServerSession(MapLink.MESSAGES.itemUrl(messageID));
		if (data.getStatusCode() == 500) {
			return null;
		}
		Message message = JsonUtils.mapJsonToType(data.getContent(), Item.get(Message.class));

		return message;
	}

	/**
	 * Perform a ping on a specified area.
	 * @param x The X coordinate
	 * @param y The Y coordinate
	 */
	public void ping(int x, int y) {
		sendPlayerEvent(SessionEvent.STAKEHOLDER_SET_LOCATION, "POINT (" + Integer.toString(x) + " "
				+ Integer.toString(y) + ")", StringUtils.TRUE);
	}

	/**
	 * Answer a popup with a specific answer.
	 * @param popup The popup to answer.
	 * @param answerID The ID of the answer to give to the popup.
	 */
	public void popupAnswerPopup(int popupID, int answerID) {
		isPlayerReady();
		sendPlayerEvent(SessionEvent.POPUP_ANSWER, Integer.toString(popupID), Integer.toString(answerID));
	}

	/**
	 * Answer a popup with a specific answer.
	 * @param message The popup to answer.
	 * @param answerID The content of the answer to give to the popup.
	 */
	public void popupAnswerPopup(Popup popup, String answer) {
		isPlayerReady();
		sendPlayerEvent(SessionEvent.POPUP_ANSWER, Integer.toString(popup.getID()),
				Integer.toString(popup.getAnswerID(answer)));
	}

	/**
	 * Answer a popup with a specific answer.
	 * @param popup The popup to answer.
	 * @param answerID The ID of the answer to give to the popup.
	 * @param date The date to provide.
	 */
	public void popupAnswerPopupDate(int popupID, int answerID, long date) {
		isPlayerReady();
		sendPlayerEvent(SessionEvent.POPUP_ANSWER_WITH_DATE, Integer.toString(popupID),
				Integer.toString(answerID), Long.toString(date));
	}

	/**
	 * Answer a popup with a specific answer.
	 * @param message The popup to answer.
	 * @param answerID The content of the answer to give to the popup.
	 * @param date The date to provide.
	 */
	public void popupAnswerPopupWithDate(Popup popup, String answer, long date) {
		isPlayerReady();
		sendPlayerEvent(SessionEvent.POPUP_ANSWER_WITH_DATE, Integer.toString(popup.getID()),
				Integer.toString(popup.getAnswerID(answer)), Long.toString(date));
	}

	/**
	 * Retrieve a specific popup.
	 * @param messageID The ID of the popup.
	 * @return The Popup.
	 */
	public Popup popupGetPopup(int popupID) {
		isPlayerReady();

		DataPackage data = dataConnector.getDataFromServerSession(MapLink.POPUPS.itemUrl(popupID));
		if (data.getStatusCode() == 500) {
			return null;
		}
		Popup popup = JsonUtils.mapJsonToType(data.getContent(), Item.get(Popup.class));

		return popup;
	}

	/**
	 * Select a stakeholder for use during the session.
	 * @param stakeholderID
	 * @return True when the stakeholder was selected successfully. False when the stakeholder was already
	 *         selected by another client.
	 * @throws IllegalArgumentException If the response from the server indicates the stakeholder ID is
	 *             invalid, this exception is thrown.
	 */

	public boolean selectStakeholder(int stakeholderID) throws IllegalArgumentException {
		isPlayerReady(false);

		DataPackage data = dataConnector.sendDataToServerSession(SessionEvent.STAKEHOLDER_SELECT,
				Integer.toString(stakeholderID), dataConnector.getClientToken());

		if (data.isContentNull()) {
			throw new IllegalArgumentException("No stakeholder with ID: " + stakeholderID);
		} else {
			if (data.isContentTrue()) {
				this.stakeholderID = stakeholderID;
			}
			return data.isContentTrue();
		}
	}

	private DataPackage sendPlayerEvent(SessionEvent event, String... params) {
		String[] newParams = new String[params.length + 1];
		newParams[0] = Integer.toString(stakeholderID);
		System.arraycopy(params, 0, newParams, 1, params.length);

		return dataConnector.sendDataToServerSession(event, newParams);
	}

	/**
	 * Set the dataconnector to use for this player.
	 * @param dataConnector The DataConnector to use.
	 */
	public void setDataConnector(DataConnector dataConnector) {
		this.dataConnector = dataConnector;
	}
}
