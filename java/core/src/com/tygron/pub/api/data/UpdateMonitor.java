package com.tygron.pub.api.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.ws.http.HTTPException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tygron.pub.api.connector.DataConnector;
import com.tygron.pub.api.connector.DataPackage;
import com.tygron.pub.api.enums.MapLink;
import com.tygron.pub.api.listeners.UpdateListenerInterface;
import com.tygron.pub.logger.Log;
import com.tygron.pub.utils.DataListUtils;
import com.tygron.pub.utils.JsonUtils;
import com.tygron.pub.utils.StringUtils;

public class UpdateMonitor {

	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class UpdateReceiverObject {
		private Map<Object, Object> items;
		private Map<Object, Object> deletes;
	}

	private boolean listening = false;
	private boolean stopListening = false;
	private boolean storeData = true;

	private DataConnector dataConnector = null;

	private Map<UpdateListenerInterface, List<String>> specificListeners = new HashMap<UpdateListenerInterface, List<String>>();
	private List<UpdateListenerInterface> generalListeners = new LinkedList<UpdateListenerInterface>();

	private final Map<String, Integer> versions = new HashMap<String, Integer>();
	private final Map<String, Map<Integer, Map>> data = new ConcurrentHashMap<String, Map<Integer, Map>>();

	{
		for (String s : MapLink.stringValues()) {
			versions.put(s, -1);
		}
	}

	/**
	 * Create a new UpdateMonitor.
	 */
	public UpdateMonitor() {
	}

	/**
	 * Create a new UpdateMonitor, and immediately link it to a DataConnector.
	 */
	public UpdateMonitor(DataConnector dataConnector) {
		setDataConnector(dataConnector);
	}

	/**
	 * Register a listener, which will be informed of updates when they occur while listening.
	 * 
	 * @param listener The Listener to add.
	 */
	public void addListener(UpdateListenerInterface listener) {
		if (!generalListeners.contains(listener) && listener != null) {
			generalListeners.add(listener);
		}
	}

	public void addListener(UpdateListenerInterface listener, String mapLinkToListenTo) {
		if (listener == null || mapLinkToListenTo == null || StringUtils.EMPTY.equals(mapLinkToListenTo)) {
			return;
		}

		List<String> mapLinks = specificListeners.get(listener);
		if (mapLinks == null) {
			mapLinks = new LinkedList<String>();
			specificListeners.put(listener, mapLinks);
		}
		if (!mapLinks.contains(mapLinkToListenTo)) {
			mapLinks.add(mapLinkToListenTo);
		}

	}

	public void addListener(UpdateListenerInterface listener, String... mapLinksToListenTo) {
		for (String mapLink : mapLinksToListenTo) {
			addListener(listener, mapLink);
		}
	}

	public void addListeners(String mapLinkToListenTo, UpdateListenerInterface... listeners) {
		for (UpdateListenerInterface listener : listeners) {
			addListener(listener, mapLinkToListenTo);
		}
	}

	private void alertListeners(final Map<String, Map<Integer, Map>> items,
			final Map<String, Map<Integer, Map>> deletes) {
		for (UpdateListenerInterface listener : generalListeners) {
			listener.update(items, deletes);
		}
		for (Entry<UpdateListenerInterface, List<String>> entry : specificListeners.entrySet()) {
			entry.getKey().update(filterDataForListener(items, entry.getValue()),
					filterDataForListener(deletes, entry.getValue()));
		}
	}

	public void clearAllListeners() {
		this.specificListeners = new HashMap<UpdateListenerInterface, List<String>>();
		this.generalListeners = new LinkedList<UpdateListenerInterface>();
	}

	public Map<String, Map<Integer, Map>> filterDataForListener(final Map<String, Map<Integer, Map>> map,
			final List<String> mapLinks) {
		Map<String, Map<Integer, Map>> returnable = new HashMap<String, Map<Integer, Map>>();

		for (String s : map.keySet()) {
			if (mapLinks.contains(s)) {
				returnable.put(s, map.get(s));
			}
		}
		return returnable;
	}

	public Map<String, Map<Integer, Map>> getData() {
		return data;
	}

	public Map<Integer, Map> getData(String mapLink) {
		return data.get(mapLink);
	}

	private Integer getVersion(String mapLink) {
		if (versions.get(mapLink) == null) {
			versions.put(mapLink, -1);
		}
		return versions.get(mapLink);
	}

	/**
	 * Is the UpdateMonitor currently listening?
	 * 
	 * @return Whether the UpdateMonitor is currently listening.
	 */
	public boolean isListening() {
		return this.listening;
	}

	/**
	 * Whether this UpdateMonitor is storing the data it received.
	 * 
	 * @return True is this UpdateMonitor is storing data, false otherwise.
	 */
	public boolean isStoringData() {
		return this.storeData;
	}

	/**
	 * Use the registered DataConnector to listen to updates and, when an update takes place, inform the
	 * listeners. Note that using this function will listen for ALL MapLinks known to this UpdateMonitor,
	 * which may take up to multple seconds to process. To listen more specifically, use
	 * listenForUpdates(Collection&lt;String&gt; mapLinksToListenTo).
	 * 
	 * @throws IllegalStateException When this UpdateMonitor is already listening for updates, the second
	 *             attempt at listening is ignored, and this exception is thrown.
	 * @throws NullPointerException When no DataConnector is registered, the attempt at listening is ignored,
	 *             and this exception is thrown.
	 * @throws HTTPException If the status code of the HTTP request is not 200, 204 or 400, this exception is
	 *             thrown and listening is halted.
	 */
	public void listenForUpdates() throws IllegalStateException, NullPointerException, HTTPException {
		listenForUpdates(versions.keySet());
	}

	/**
	 * Use the registered DataConnector to listen to updates and, when an update takes place, inform the
	 * listeners.
	 * 
	 * @param mapLinksToListenTo A Collection indicating which MapLinks to listen for.
	 * @throws IllegalStateException When this UpdateMonitor is already listening for updates, the second
	 *             attempt at listening is ignored, and this exception is thrown.
	 * @throws NullPointerException When no DataConnector is registered, the attempt at listening is ignored,
	 *             and this exception is thrown.
	 * @throws HTTPException If the status code of the HTTP request is not 200, 204 or 400, this exception is
	 *             thrown and listening is halted.
	 */
	public void listenForUpdates(final Collection<String> mapLinksToListenTo) throws IllegalStateException,
			NullPointerException, HTTPException {
		synchronized (this) {
			if (listening) {
				throw new IllegalStateException("Already listening for updates.");
			}
			if (dataConnector == null) {
				throw new NullPointerException("No DataConnector connected to UpdateMonitor.");
			}
			listening = true;
		}

		HashMap<String, Integer> mapLinksForListening = new HashMap<String, Integer>();

		try {

			while (listening && !stopListening) {

				mapLinksForListening.clear();
				synchronized (this) {
					for (String mapLink : mapLinksToListenTo) {
						mapLinksForListening.put(mapLink, getVersion(mapLink));
					}
				}

				if (dataConnector.getServerSlot() == null) {
					listening = false;
					stopListening = false;
					throw new IllegalStateException("The DataConnector's serverslot is null.");
				}

				DataPackage data = null;

				try {
					Log.verbose("Beginning new update call");
					data = dataConnector.sendUpdateRequestToServerSession(mapLinksForListening);
				} catch (NullPointerException e) {
					throw new IllegalStateException(
							"DataConnector is not in correct state to perform requests. Check that a server address and server slot have been registered.",
							e);
				} catch (Exception e) {
					throw new IllegalStateException(
							"Something has occured which prevented the dataConnector from connecting.", e);
				}

				Log.verbose("Update status code: " + data.getStatusCode());

				switch (data.getStatusCode()) {
					case 200:
						processDataAndAlertListeners(data.getContent());
					case 204:
						break;
					case 400:
						if (!stopListening) {
							throw new IllegalStateException(
									"There is a problem with the request. It's possible the ServerToken is no longer valid, the server address has changed or the session has expired.");
						}
						Log.verbose("Request has failed, but it's been announced to stop listening, so it's probably just the session closing.");
						break;
					default:
						throw new HTTPException(data.getStatusCode());
				}
			}
		} catch (Exception e) {
			listening = false;
			stopListening = false;
			throw e;
		}
		listening = false;
		stopListening = false;
	}

	/**
	 * This function receives a Map of data, as returned by the update function of the API. This function then
	 * collapses any unneccesary depth, and retrieves the data stored within, one item at a time. If the
	 * UpdateMonitor is set to store data, the data is stored as well. When completed, a new map is returned
	 * containing a map per mapLink of item (MapLink), wherein the submap contains each item indexed by its
	 * id.
	 * 
	 * @param map The map to process, as returned by the API's update function.
	 * @param delete When storing data, whether to delete the data or just store it. This is ignored if
	 * @return The cleaned map with MapLinks as keys, and Maps as values. The submaps are Maps of items,
	 *         indexed by id.
	 */
	public Map<String, Map<Integer, Map>> processData(Map<Object, Object> map, boolean delete) {
		// The map we return should enforce some amount of type safety. For this
		// reason it is typed, and items
		// are only added to it when they are confirmed to conform to the
		// required types.

		HashMap<String, Map<Integer, Map>> returnable = new HashMap<String, Map<Integer, Map>>();

		// At this time, the returned data structure contains unneccesary nesting. To make the data easier to
		// use, we collapse it.
		Map<Object, Object> collapsedData = DataListUtils.collapseMapsInListsInMapsInMap(map);

		for (Object mapLinkObject : collapsedData.keySet()) {
			if (!(mapLinkObject instanceof String)) {
				Log.warning("Failed to retrieve MapLink as String: " + mapLinkObject.toString());
				continue;
			}
			String mapLink = (String) mapLinkObject;

			Object mapLinkValue = collapsedData.get(mapLink);
			if (!(mapLinkValue instanceof List<?>)) {
				Log.warning("Failed to retrieve Items for a specific MapLinks as List: " + mapLink);
				continue;
			}

			List<?> itemList = (List<?>) mapLinkValue;
			HashMap<Integer, Map> itemMap = processData(mapLink, itemList, delete);

			returnable.put(mapLink, itemMap);
		}

		return returnable;
	}

	/**
	 * This function receives a List of data, specifically of a single MapLink. The items in the List are
	 * parsed and placed into a map where they are indexed with their id's as key. If the UpdateMonitor is set
	 * to store data, the data is stored as well. When completed, a new map is returned containing each item
	 * indexed by its id.
	 * 
	 * @param map The map to process, as returned by the API's update function.
	 * @param delete When storing data, whether to delete the data or just store it. This is ignored if
	 * @return A map with items as values, indexed by ids.
	 */
	public HashMap<Integer, Map> processData(String mapLink, List<?> itemList, boolean delete) {
		// The map we return should enforce some amount of type safety. For this
		// reason is it typed, and items
		// are only added to it when they are confirmed to conform to the
		// required types.

		HashMap<Integer, Map> returnable = new HashMap<Integer, Map>();

		List<Object> collapsedData = DataListUtils.collapseMapsInList(itemList);

		try {
			for (Object itemObject : collapsedData) {
				if (!(itemObject instanceof Map<?, ?>)) {
					Log.warning("Failed to read Item as Map: " + itemObject.toString());
					continue;
				}

				Map<?, ?> item = (Map<?, ?>) itemObject;
				Object itemIdObject = item.get("id");
				if ((itemIdObject == null) || (!(itemIdObject instanceof Integer))) {
					Log.warning("Failed to retrieve Item Id from:" + item.toString());
					continue;
				}
				Integer itemId = (Integer) itemIdObject;

				Object itemVersionObject = item.get("version");
				if ((itemVersionObject == null) || (!(itemVersionObject instanceof Integer))) {
					Log.warning("Failed to retrieve Item version from:" + mapLink + ": " + itemId);
					// continue; //We may not have a version number, but
					// the item is otherwise sound, so
					// we'll store it.
				} else {
					Integer itemVersion = (Integer) itemVersionObject;
					versions.put(mapLink, Math.max(getVersion(mapLink), itemVersion));
				}

				returnable.put(itemId, item);
				if (storeData) {
					storeData(mapLink, itemId, item, delete);
				}
			}
		} catch (ClassCastException e) {
			Log.exception(e, "Failed to read part of the updated data.");
		} catch (NullPointerException e) {
			Log.exception(e, "Encountered Null as part of updated data.");
		} catch (Exception e) {
			Log.exception(e, "An unexpected condition occured while parsing updated data.");
		}

		return returnable;
	}

	private void processDataAndAlertListeners(String JSon) {
		UpdateReceiverObject received = JsonUtils.mapJsonToType(JSon, UpdateReceiverObject.class);

		Map<String, Map<Integer, Map>> items = processData(
				DataListUtils.collapseMapsInListsInMapsInMap(received.items), false);
		Map<String, Map<Integer, Map>> deletes = processData(
				DataListUtils.collapseMapsInListsInMapsInMap(received.deletes), true);

		alertListeners(items, deletes);

	}

	/**
	 * Remove a registered listener. The listener will no longer be informed of updates.
	 * 
	 * @param listener The listener to remove.
	 */
	public void removeListener(UpdateListenerInterface listener) {
		if (generalListeners.contains(listener)) {
			generalListeners.remove(listener);
		}
	}

	public void removeListener(UpdateListenerInterface listener, String mapLink) {
		if (listener == null || mapLink == null || StringUtils.EMPTY.equals(mapLink)) {
			return;
		}

		List<String> mapLinks = specificListeners.get(listener);
		if (mapLinks == null) {
			return;
		}
		if (mapLinks.contains(mapLink)) {
			mapLinks.remove(mapLink);
		}
		if (mapLinks.size() == 0) {
			specificListeners.remove(listener);
		}
	}

	/**
	 * Set the dataconnector to use to listen to updates.
	 * 
	 * @param dataConnector
	 */
	public void setDataConnector(DataConnector dataConnector) {
		this.dataConnector = dataConnector;
	}

	/**
	 * Set whether this UpdateMonitor should store data when it is received.
	 * 
	 * @param storeData Whether to store data.
	 */
	public void setStoringData(boolean storeData) {
		this.storeData = storeData;
	}

	/**
	 * Stop the listening process. The UpdateMonitor will finish its current listening cycle and alert the
	 * listeners as needed, and then stop.
	 */
	public synchronized void stopListening() {
		stopListening = true;
		if (listening == false) {
			stopListening = false;
		}
	}

	private void storeData(String mapLink, Integer id, Map dataMap, boolean delete) {
		if (mapLink == null || id == null) {
			Log.warning("Failed to store data because the following was null:"
					+ (mapLink == null ? " Maplink" : "") + (id == null ? " id" : ""));
			return;
		}

		Map<Integer, Map> itemMap = data.get(mapLink);
		if (itemMap == null) {
			itemMap = new HashMap<Integer, Map>();
			data.put(mapLink, itemMap);
		}
		if (delete) {
			itemMap.remove(id);
		} else {
			itemMap.put(id, dataMap);
		}
	}
}
