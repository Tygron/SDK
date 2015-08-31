package com.tygron.pub.api.connector.modules;

import java.util.List;
import java.util.Map;
import com.tygron.pub.api.connector.DataConnector;
import com.tygron.pub.api.connector.DataPackage;
import com.tygron.pub.api.data.item.Building;
import com.tygron.pub.api.data.item.Function;
import com.tygron.pub.api.data.item.FunctionOverride;
import com.tygron.pub.api.data.item.Indicator;
import com.tygron.pub.api.data.item.Item;
import com.tygron.pub.api.data.item.Message;
import com.tygron.pub.api.data.item.Popup;
import com.tygron.pub.api.enums.MapLink;
import com.tygron.pub.logger.Log;
import com.tygron.pub.utils.DataUtils;
import com.tygron.pub.utils.JsonUtils;

public class DataModule {
	private DataConnector dataConnector = null;

	/**
	 * Create a new DataModule, and immediately link it to a DataConnector.
	 */
	public DataModule(DataConnector dataConnector) {
		setDataConnector(dataConnector);
	}

	/**
	 * Retrieve a specific building.
	 * @param messageID The ID of the building.
	 * @return The Building.
	 */
	public Building getBuilding(int buildingID) {
		return itemGetItem(buildingID, MapLink.BUILDINGS, Building.class);
	}

	/**
	 * Retrieve buildings.
	 * @return The Buildings.
	 */
	public Map<Integer, Building> getBuildings() {
		return itemGetItems(MapLink.BUILDINGS, Building.class);
	}

	/**
	 * Get the dataConnector of the module.
	 * @return The DataConnector of the module.
	 */
	public DataConnector getDataConnector() {
		return this.dataConnector;
	}

	/**
	 * Retrieve a specific function.
	 * @param messageID The ID of the function.
	 * @return The Function.
	 */
	public Function getFunction(int functionID) {
		return itemGetItem(functionID, MapLink.FUNCTIONS, Function.class);
	}

	/**
	 * Retrieve a specific function override.
	 * @param messageID The ID of the function override.
	 * @return The FunctionOverride.
	 */
	public FunctionOverride getFunctionOverride(int functionOverrideID) {
		return itemGetItem(functionOverrideID, MapLink.FUNCTION_OVERRIDES, FunctionOverride.class);
	}

	/**
	 * Retrieve function overrides.
	 * @return The FunctionOverrides.
	 */
	public Map<Integer, FunctionOverride> getFunctionOverrides() {
		return itemGetItems(MapLink.FUNCTION_OVERRIDES, FunctionOverride.class);
	}

	/**
	 * Retrieve functions.
	 * @return The Functions.
	 */
	public Map<Integer, Function> getFunctions() {
		return itemGetItems(MapLink.FUNCTIONS, Function.class);
	}

	/**
	 * Retrieve a specific indicator.
	 * @param indicatorID The ID of the indicator.
	 * @return The Indicator.
	 */
	public Indicator getIndicator(int indicatorID) {
		return itemGetItem(indicatorID, MapLink.INDICATORS, Indicator.class);
	}

	/**
	 * Retrieve indicators.
	 * @return The Indicators.
	 */
	public Map<Integer, Indicator> getIndicators() {
		return itemGetItems(MapLink.INDICATORS, Indicator.class);
	}

	/**
	 * Retrieve a specific message.
	 * @param messageID The ID of the message.
	 * @return The Message.
	 */
	public Message getMessage(int messageID) {
		return itemGetItem(messageID, MapLink.MESSAGES, Message.class);
	}

	/**
	 * Retrieve messages.
	 * @return The Messages.
	 */
	public Map<Integer, Message> getMessages() {
		return itemGetItems(MapLink.MESSAGES, Message.class);
	}

	/**
	 * Retrieve a specific popup.
	 * @param messageID The ID of the popup.
	 * @return The Popup.
	 */
	public Popup getPopup(int popupID) {
		return itemGetItem(popupID, MapLink.POPUPS, Popup.class);
	}

	/**
	 * Retrieve popups
	 * @return The Popups.
	 */
	public Map<Integer, Popup> getPopups() {
		return itemGetItems(MapLink.POPUPS, Popup.class);
	}

	protected boolean isModuleReady() {
		if (dataConnector == null || dataConnector.getServerSlot() == null
				|| dataConnector.getServerToken() == null) {
			throw new IllegalStateException("The registered DataConnector is not ready");
		}
		return true;
	}

	protected final <T extends Item> T itemGetItem(int ID, MapLink maplink, Class<T> itemType) {
		isModuleReady();

		DataPackage data = dataConnector.getDataFromServerSession(maplink.itemUrl(ID));
		if (data.getStatusCode() == 500) {
			return null;
		}
		T item = JsonUtils.mapJsonToType(data.getContent(), Item.get(itemType));

		return item;
	}

	protected final <T extends Item> Map<Integer, T> itemGetItems(MapLink maplink, Class<T> itemType) {
		isModuleReady();

		Map<Integer, T> functions = null;

		DataPackage data = dataConnector.getDataFromServerSession(maplink);

		try {
			List<?> itemList = JsonUtils.mapJsonToList(data.getContent());
			functions = DataUtils.dataListToItemMap((List<Map<?, ?>>) itemList, itemType);
		} catch (NullPointerException e) {
			Log.exception(e, "Nullpointer has occured.");
			return null;
		} catch (IllegalArgumentException e) {
			Log.exception(e, "Failed to parse to item.");
			return null;
		} catch (ClassCastException e) {
			Log.exception(e, "Failed to cast.");
			return null;
		}

		return functions;
	}

	/**
	 * Set the dataconnector to use for this player.
	 * @param dataConnector The DataConnector to use.
	 */
	public void setDataConnector(DataConnector dataConnector) {
		this.dataConnector = dataConnector;
	}
}
