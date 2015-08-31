package com.tygron.pub.utils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.tygron.pub.api.connector.DataConnector;
import com.tygron.pub.api.connector.DataPackage;
import com.tygron.pub.api.data.item.Building;
import com.tygron.pub.api.data.item.CodedEvent;
import com.tygron.pub.api.data.item.Function;
import com.tygron.pub.api.data.item.FunctionOverride;
import com.tygron.pub.api.data.item.Item;
import com.tygron.pub.api.data.item.Message;
import com.tygron.pub.api.data.item.Message.MessageAnswer;
import com.tygron.pub.api.data.item.Popup;
import com.tygron.pub.api.data.item.Stakeholder;
import com.tygron.pub.api.enums.MapLink;
import com.tygron.pub.api.enums.events.AnswerEvent;
import com.tygron.pub.api.enums.events.SessionEvent;
import com.tygron.pub.logger.Log;

public class GamePlayUtils {

	public static void answerMessage(int stakeholderID, int messageID, int answerID,
			DataConnector dataConnector) {
		dataConnector.sendDataToServerSession(SessionEvent.MESSAGE_ANSWER, Integer.toString(stakeholderID),
				Integer.toString(messageID), Integer.toString(answerID));
	}

	public static Building getBuilding(int ownerID, String location, int functionID, int floors,
			Collection<Building> buildings) {
		try {
			for (Building building : getBuildings(ownerID, location, functionID, floors, buildings)) {
				return building;
			}
		} catch (IndexOutOfBoundsException e) {
		}
		return null;
	}

	public static Building getBuilding(int ownerID, String location, int functionID, int floors,
			DataConnector dataConnector) {
		try {
			for (Building building : getBuildings(ownerID, location, functionID, floors, dataConnector)) {
				return building;
			}
		} catch (IndexOutOfBoundsException e) {
		}
		return null;
	}

	public static Collection<Building> getBuildingDecendents(int parentID, Collection<Building> buildings) {
		Collection<Building> returnable = new LinkedList<Building>();

		for (Building currentBuilding : buildings) {
			if (!(currentBuilding.getPredecessorID() == parentID)) {
				continue;
			}
			returnable.add(currentBuilding);
		}
		return returnable;
	}

	public static Collection<Building> getBuildingDecendents(int parentID, DataConnector dataConnector) {
		Map<Integer, Building> buildings = null;
		DataPackage data = dataConnector.getDataFromServerSession(MapLink.BUILDINGS);

		try {
			List<?> buildingsList = JsonUtils.mapJsonToList(data.getContent());
			buildings = DataUtils.dataListToItemMap((List<Map<?, ?>>) buildingsList, Building.class);
		} catch (NullPointerException e) {
			return null;
		} catch (IllegalArgumentException e) {
			return null;
		} catch (ClassCastException e) {
			return null;
		}
		return getBuildingDecendents(parentID, buildings.values());
	}

	public static Collection<Building> getBuildings(int ownerID, String location, int functionID, int floors,
			Collection<Building> buildings) {
		Collection<Building> returnable = new LinkedList<Building>();

		for (Building currentBuilding : buildings) {
			if (!StringUtils.isEmpty(location)) {
				if (!(currentBuilding.getPolygons().equals(location))) {
					continue;
				}
			}
			if (ownerID != StringUtils.NOTHING) {
				if (!(currentBuilding.getOwnerID() == ownerID)) {
					continue;
				}
			}
			if (functionID != StringUtils.NOTHING) {
				if (!(currentBuilding.getFunctionID() == functionID)) {
					continue;
				}
			}

			if (floors != StringUtils.NOTHING) {
				if (!(currentBuilding.getFloors() == floors)) {
					continue;
				}
			}
			returnable.add(currentBuilding);
		}
		return returnable;
	}

	public static Collection<Building> getBuildings(int ownerID, String location, int functionID, int floors,
			DataConnector dataConnector) {
		Map<Integer, Building> buildings = null;
		DataPackage data = dataConnector.getDataFromServerSession(MapLink.BUILDINGS);

		try {
			List<?> buildingsList = JsonUtils.mapJsonToList(data.getContent());
			buildings = DataUtils.dataListToItemMap((List<Map<?, ?>>) buildingsList, Building.class);
		} catch (NullPointerException e) {
			Log.warning("Data from which to get buiildings was null.");
			return null;
		} catch (IllegalArgumentException e) {
			Log.warning("Data from which to get buildings could not be parsed.");
			return null;
		} catch (ClassCastException e) {
			Log.warning("Data was of unexpected type.");
			return null;
		}
		return getBuildings(ownerID, location, functionID, floors, buildings.values());
	}

	public static int getFloorDefaultForFunction(int functionID, DataConnector dataConnector) {
		return getFloorRangeForFunction(functionID, dataConnector)[2];
	}

	public static int getFloorMaxForFunction(int functionID, DataConnector dataConnector) {
		return getFloorRangeForFunction(functionID, dataConnector)[1];
	}

	public static int getFloorMinForFunction(int functionID, DataConnector dataConnector) {
		return getFloorRangeForFunction(functionID, dataConnector)[0];
	}

	public static int[] getFloorRangeForFunction(int functionID, DataConnector dataConnector) {
		DataPackage data = dataConnector.getDataFromServerSession(MapLink.FUNCTIONS.itemUrl(functionID));
		Function function = Item.mapJsonToItem(data.getContent(), Function.class);
		data = dataConnector.getDataFromServerSession(MapLink.FUNCTION_OVERRIDES.itemUrl(functionID));
		FunctionOverride functionOverride = Item.mapJsonToItem(data.getContent(), FunctionOverride.class);

		return new int[] { function.getFloorsMin(functionOverride), function.getFloorsMax(functionOverride),
				function.getFloorsDefault(functionOverride) };
	}

	public static List<Message> getMessagesRelatedToBuilding(Building building, Collection<Message> messages) {
		List<Message> returnable = new LinkedList<Message>();

		if (building == null) {
			return returnable;
		}

		for (Message currentMessage : messages) {
			messageAnswerLoop: for (MessageAnswer answer : currentMessage.getAnswers()) {
				for (CodedEvent event : answer.getEvents()) {
					try {
						String eventType = event.getSimpleEventType();
						if (AnswerEvent.UPGRADE_APPROVAL.toString().equals(eventType)) {
							if (event.getParameters().get(2).equals(building.getID())) {
								returnable.add(currentMessage);
								break messageAnswerLoop;
							}
						}
					} catch (IndexOutOfBoundsException e) {
					}
				}
			}
		}

		return returnable;
	}

	public static List<Message> getMessagesRelatedToBuilding(Building building, DataConnector dataConnector) {
		Map<Integer, Message> messages = null;
		DataPackage data = dataConnector.getDataFromServerSession(MapLink.MESSAGES);

		try {
			List<?> messagesList = JsonUtils.mapJsonToList(data.getContent());
			messages = DataUtils.dataListToItemMap((List<Map<?, ?>>) messagesList, Message.class);
		} catch (NullPointerException e) {
			return null;
		} catch (IllegalArgumentException e) {
			return null;
		} catch (ClassCastException e) {
			return null;
		}
		return getMessagesRelatedToBuilding(building, messages.values());
	}

	public static List<Message> getMessagesRelatedToBuilding(int ownerID, String location, int functionID,
			int floors, DataConnector dataConnector) {
		Building building = getBuilding(ownerID, location, functionID, floors, dataConnector);

		return getMessagesRelatedToBuilding(building, dataConnector);
	}

	public static List<Stakeholder> getPlayableStakeholders(Collection<Stakeholder> stakeholders) {
		List<Stakeholder> returnable = new LinkedList<Stakeholder>();
		if (stakeholders == null) {
			return returnable;
		}
		for (Stakeholder stakeholder : stakeholders) {
			if (stakeholder.isPlayable()) {
				returnable.add(stakeholder);
			}
		}
		return returnable;
	}

	public static List<Stakeholder> getPlayableStakeholders(DataConnector dataConnector) {
		Map<Integer, Stakeholder> stakeholders = null;
		DataPackage data = dataConnector.getDataFromServerSession(MapLink.STAKEHOLDERS);

		try {
			List<?> stakeholderList = JsonUtils.mapJsonToList(data.getContent());
			stakeholders = DataUtils.dataListToItemMap((List<Map<?, ?>>) stakeholderList, Stakeholder.class);
		} catch (NullPointerException e) {
			return null;
		} catch (IllegalArgumentException e) {
			return null;
		} catch (ClassCastException e) {
			return null;
		}
		return getPlayableStakeholders(stakeholders.values());
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
			popups = DataUtils.dataListToItemMap((List<Map<?, ?>>) popupsList, Popup.class);
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
