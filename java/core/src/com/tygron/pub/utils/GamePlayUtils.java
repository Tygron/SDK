package com.tygron.pub.utils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.tygron.pub.api.connector.DataConnector;
import com.tygron.pub.api.connector.DataPackage;
import com.tygron.pub.api.data.item.Building;
import com.tygron.pub.api.data.item.Popup;
import com.tygron.pub.api.enums.MapLink;

public class GamePlayUtils {

	public static Building getBuilding(int ownerID, String location, int functionID, int floors,
			Collection<Building> buildings) {

		for (Building currentBuilding : buildings) {
			if (!(currentBuilding.getPolygons().equals(location))) {
				continue;
			}
			if (!(currentBuilding.getOwnerID() == ownerID)) {
				continue;
			}
			if (!(currentBuilding.getFunctionTypeID() == functionID)) {
				continue;
			}
			if (!(currentBuilding.getFloors() == floors)) {
				continue;
			}
			return currentBuilding;
		}
		return null;
	}

	public static Building getBuilding(int ownerID, String location, int functionID, int floors,
			DataConnector dataConnector) {
		Map<Integer, Building> buildings = null;
		DataPackage data = dataConnector.getDataFromServerSession(MapLink.BUILDINGS);

		try {
			List<?> buildingsList = JsonUtils.mapJsonToList(data.getContent());
			buildings = DataUtils.dataListToItemMap((List<Map<String, Map<?, ?>>>) buildingsList,
					Building.class);
		} catch (NullPointerException e) {
			return null;
		} catch (IllegalArgumentException e) {
			return null;
		} catch (ClassCastException e) {
			return null;
		}
		return getBuilding(ownerID, location, functionID, floors, buildings.values());
	}

	public static List<Popup> getPopupsRelatedToBuilding(Building building, Collection<Popup> popups) {
		List<Popup> returnable = new LinkedList<Popup>();

		if (building == null) {
			return returnable;
		}

		for (Popup currentPopup : popups) {
			if (!(currentPopup.getLinkID() == building.getID())) {
				continue;
			}
			if (!currentPopup.getLinkType().equals(MapLink.BUILDINGS.toString())) {
				continue;
			}
			returnable.add(currentPopup);
		}

		return returnable;
	}

	public static List<Popup> getPopupsRelatedToBuilding(Building building, DataConnector dataConnector) {
		Map<Integer, Popup> popups = null;
		DataPackage data = dataConnector.getDataFromServerSession(MapLink.POPUPS);

		try {
			List<?> popupsList = JsonUtils.mapJsonToList(data.getContent());
			popups = DataUtils.dataListToItemMap((List<Map<String, Map<?, ?>>>) popupsList, Popup.class);
		} catch (NullPointerException e) {
			return null;
		} catch (IllegalArgumentException e) {
			return null;
		} catch (ClassCastException e) {
			return null;
		}
		return getPopupsRelatedToBuilding(building, popups.values());
	}

	public static List<Popup> getPopupsRelatedToBuilding(int ownerID, String location, int functionID,
			int floors, DataConnector dataConnector) {
		Building building = getBuilding(ownerID, location, functionID, floors, dataConnector);

		return getPopupsRelatedToBuilding(building, dataConnector);
	}
}
