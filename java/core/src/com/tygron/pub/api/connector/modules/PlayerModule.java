package com.tygron.pub.api.connector.modules;

import com.tygron.pub.api.connector.DataConnector;
import com.tygron.pub.api.connector.DataPackage;
import com.tygron.pub.api.data.item.Building;
import com.tygron.pub.api.data.item.Message;
import com.tygron.pub.api.data.item.Popup;
import com.tygron.pub.api.data.item.UpgradeType;
import com.tygron.pub.api.data.misc.GeometryObject;
import com.tygron.pub.api.enums.MapLink;
import com.tygron.pub.api.enums.events.SessionEvent;
import com.tygron.pub.utils.JsonUtils;
import com.tygron.pub.utils.StringUtils;

public class PlayerModule extends DataModule {
	private int stakeholderID = StringUtils.NOTHING;

	/**
	 * Create a new PlayerModule, and immediately link it to a DataConnector.
	 */
	public PlayerModule(DataConnector dataConnector) {
		super(dataConnector);
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
				Integer.toString(floors), JsonUtils.mapJsonToMap(location));
	}

	/**
	 * Plan the demolition of a building.
	 * @param building The building to demolish.
	 */
	public void buildingPlanDemolish(Building building) {
		isPlayerReady();

		sendPlayerEvent(SessionEvent.BUILDING_PLAN_DEMOLISH, Integer.toString(building.getID()));
	}

	/**
	 * Plan the demolition of a building.
	 * @param buildingID The building to demolish.
	 */
	public void buildingPlanDemolish(int buildingID) {
		isPlayerReady();

		sendPlayerEvent(SessionEvent.BUILDING_PLAN_DEMOLISH, Integer.toString(buildingID));
	}

	/**
	 * Plan the demolition of a building.
	 * @param locationString A multipolygon description indicating the location to demolish.
	 */
	public void buildingPlanDemolish(String location) {
		isPlayerReady();

		sendPlayerEvent(SessionEvent.BUILDING_PLAN_DEMOLISH_COORDINATES, JsonUtils.mapJsonToMap(location),
				"SURFACE");
	}

	/**
	 * Plan the demolition of an underground construction.
	 * @param locationString A multipolygon description indicating the location to demolish.
	 */
	public void buildingPlanDemolishUnderground(String location) {
		isPlayerReady();

		sendPlayerEvent(SessionEvent.BUILDING_PLAN_DEMOLISH_COORDINATES, JsonUtils.mapJsonToMap(location),
				"UNDERGROUND");
	}

	/**
	 * Plan the revertion of actions in an area.
	 * @param locationString A multipolygon description indicating the location to revert.
	 */
	public void buildingPlanRevert(String location) {
		isPlayerReady();

		sendPlayerEvent(SessionEvent.BUILDING_REVERT_POLYGON, JsonUtils.mapJsonToMap(location));
	}

	/**
	 * Plan the upgrade of a building.
	 * @param upgradeID The id of the upgrade for the building (the building type).
	 * @param location A multipolygon String.
	 */
	public void buildingPlanUpgrade(int upgradeID, Building building) {
		isPlayerReady();

		sendPlayerEvent(SessionEvent.BUILDING_PLAN_UPGRADE, Integer.toString(upgradeID),
				building.getPolygons());
	}

	/**
	 * Plan the upgrade of a building.
	 * @param upgradeID The id of the upgrade for the building (the building type).
	 * @param location A multipolygon String.
	 */
	public void buildingPlanUpgrade(int upgradeID, String location) {
		isPlayerReady();

		sendPlayerEvent(SessionEvent.BUILDING_PLAN_UPGRADE, Integer.toString(upgradeID),
				JsonUtils.mapJsonToMap(location));
	}

	/**
	 * Get the current StakeholderID.
	 * @return The player's stakeholderID.
	 */
	public int getStakeholderID() {
		return stakeholderID;
	}

	/**
	 * Retrieve a specific upgrade type.
	 * @param messageID The ID of the upgrade.
	 * @return The Upgrade.
	 */
	public UpgradeType getUpgrade(int upgradeID) {
		return itemGetItem(upgradeID, MapLink.UPGRADE_TYPES, UpgradeType.class);
	}

	protected boolean isPlayerReady() {
		return isPlayerReady(true);
	}

	protected boolean isPlayerReady(boolean stakeholder) {
		if ((!isModuleReady()) || getDataConnector() == null
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
	 * Perform a ping on a specified area.
	 * @param x The X coordinate
	 * @param y The Y coordinate
	 */
	public void ping(int x, int y) {
		isPlayerReady();
		sendPlayerEvent(
				SessionEvent.STAKEHOLDER_SET_LOCATION,
				GeometryObject.getGeometryObject("Point",
						"[" + Integer.toString(x) + ", " + Integer.toString(y) + "]"), StringUtils.TRUE);
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
	public void popupAnswerPopupWithDate(int popupID, int answerID, long date) {
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
	 * Select a stakeholder for use during the session.
	 * @param stakeholderID The stakeholder to select.
	 * @return True when the stakeholder was selected successfully. False when the stakeholder was already
	 *         selected by another client.
	 * @throws IllegalArgumentException If the response from the server indicates the stakeholder ID is
	 *             invalid, this exception is thrown.
	 */

	public boolean selectStakeholder(int stakeholderID) throws IllegalArgumentException {
		isPlayerReady(false);

		DataPackage data = getDataConnector().sendDataToServerSession(SessionEvent.STAKEHOLDER_SELECT,
				Integer.toString(stakeholderID), getDataConnector().getClientToken());

		if (data.isContentNull()) {
			throw new IllegalArgumentException("No stakeholder with ID: " + stakeholderID);
		} else {
			if (data.isContentTrue()) {
				this.stakeholderID = stakeholderID;
			}
			return data.isContentTrue();
		}
	}

	/**
	 * This function prepends the call to an event with the stakeholder's ID
	 * @param event The event to fire.
	 * @param params The parameters (excluding the ID of the enacting stakeholder.
	 * @return The DataPackage resulting from the call.
	 */
	private DataPackage sendPlayerEvent(SessionEvent event, Object... params) {
		Object[] newParams = new Object[params.length + 1];
		newParams[0] = Integer.toString(stakeholderID);
		System.arraycopy(params, 0, newParams, 1, params.length);

		return getDataConnector().sendDataToServerSession(event, newParams);
	}
}
