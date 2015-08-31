package com.tygron.pub.api.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.ws.rs.ProcessingException;
import javax.xml.ws.http.HTTPException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tygron.pub.api.connector.DataConnector;
import com.tygron.pub.api.connector.DataPackage;
import com.tygron.pub.api.listeners.UpdateListenerInterface;
import com.tygron.pub.logger.Log;
import com.tygron.pub.utils.DataUtils;
import com.tygron.pub.utils.JsonUtils;
import com.tygron.pub.utils.StringUtils;

/**
 * This class serves as a mechanism to listen to a session. Whenever new data presents itself, it is
 * immediately processed so that it can be easily accessed, saved if desired, and sent to any parts of the
 * application that have subscribed to it.
 *
 * By default, all data retrieved is automatically stored in a DataMonitor, created when the UpdateMonitor is
 * instantiated. If desirable you can overwrite the DataMonitor with your own implementation. It is possible
 * to set the UpdateMonitor to skip storing data. A DataMonitor will still be required to store the most
 * recent version of the data listened to so far.
 *
 * This mechanism relies on a DataConnector to connect to the server. To set up the UpdateMonitor and start
 * listening to updates, you can do the following:
 *
 * <pre>
 * UpdateMonitor monitor = new UpdateMonitor();
 * monitor.setDataConnector(yourDataConnector);
 * monitor.addListener(yourUpdateListenerImplementation);
 * new Thread(new Runnable() {
 * 	public void run() {
 * 		monitor.startListening();
 * 	}
 * }).start();
 * </pre>
 * @author Rudolf
 *
 */
public class UpdateMonitor {

	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class UpdateReceiverObject {
		private Map<String, List<Map<?, ?>>> items;
		private Map<String, List<Map<?, ?>>> deletes;
	}

	private boolean listening = false;
	private boolean stopListening = false;
	private boolean storeData = true;

	private DataConnector dataConnector = null;
	private DataMonitor dataMonitor = new DataMonitor();

	private Map<UpdateListenerInterface, List<String>> specificListeners = new HashMap<UpdateListenerInterface, List<String>>();
	private List<UpdateListenerInterface> generalListeners = new LinkedList<UpdateListenerInterface>();

	private final Collection<String> mapLinksToListenTo = new LinkedList<String>();

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
	 * Register a listener, which will be informed of updates when they occur while listening. This listener
	 * will automatically listen to all updates.
	 *
	 * @param listener The Listener to add.
	 */
	public void addListener(UpdateListenerInterface listener) {
		if (!generalListeners.contains(listener) && listener != null) {
			generalListeners.add(listener);
		}
	}

	/**
	 * Register a listener, which will be informed of updates of the specified mapLink when they occur while
	 * listening.
	 *
	 * @param listener The Listener to add.
	 * @param mapLinkToListenTo The type of data to listen to.
	 */
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

	/**
	 * Register a listener, which will be informed of updates of the specified mapLinks when they occur while
	 * listening.
	 *
	 * @param listener The Listener to add.
	 * @param mapLinkToListenTo The types of data to listen to.
	 */
	public void addListener(UpdateListenerInterface listener, String... mapLinksToListenTo) {
		for (String mapLink : mapLinksToListenTo) {
			addListener(listener, mapLink);
		}
	}

	/**
	 * Register listeners, which will be informed of updates of the specified mapLink when they occur while
	 * listening.
	 *
	 * @param mapLinkToListenTo The type of data to listen to.
	 * @param listeners The Listeners to add.
	 */
	public void addListeners(String mapLinkToListenTo, UpdateListenerInterface... listeners) {
		for (UpdateListenerInterface listener : listeners) {
			addListener(listener, mapLinkToListenTo);
		}
	}

	private void alertListeners(final Map<String, Map<Integer, Map<?, ?>>> items,
			final Map<String, Map<Integer, Map<?, ?>>> deletes) {
		for (UpdateListenerInterface listener : generalListeners) {
			listener.update(items, deletes);
		}
		for (Entry<UpdateListenerInterface, List<String>> entry : specificListeners.entrySet()) {
			entry.getKey().update(filterDataForListener(items, entry.getValue()),
					filterDataForListener(deletes, entry.getValue()));
		}
	}

	/**
	 * Remove all listeners from the UpdateMonitor. The Listeners are not informed of this.
	 */
	public void clearAllListeners() {
		this.specificListeners = new HashMap<UpdateListenerInterface, List<String>>();
		this.generalListeners = new LinkedList<UpdateListenerInterface>();
	}

	/**
	 * Create a new mapping, which only contains ItemMaps of the MapLinks specified. This function is
	 * non-destructive; the original map is unchanged. However, changes in the ItemMaps in the returned map
	 * are reflected in the original map and vice-versa.
	 * @param map A map of ItemMaps, from which appropriate data should be retrieved.
	 * @param mapLinks The MapLinks to carry over from the provided map to the returned map.
	 * @return A map containing a subset of the provided map. Effectively the intersection between the
	 *         provided maps and the maplinks.
	 */
	public Map<String, Map<Integer, Map<?, ?>>> filterDataForListener(
			final Map<String, Map<Integer, Map<?, ?>>> map, final List<String> mapLinks) {
		Map<String, Map<Integer, Map<?, ?>>> returnable = new HashMap<String, Map<Integer, Map<?, ?>>>();

		for (String s : map.keySet()) {
			if (mapLinks.contains(s)) {
				returnable.put(s, map.get(s));
			}
		}
		return returnable;
	}

	/**
	 * Get the DataMonitor currently used to store data and version information.
	 * @return The used DataMonitor.
	 */
	public DataMonitor getDataMonitor() {
		return this.dataMonitor;
	}

	/**
	 * Get a Collection of Strings indicating what MapLinks the UpdateMonitor is currently listening to.
	 * @return A Collection of Strings, matching the MapLink enums, which the UpdateMonitor is listening to.
	 */
	public Collection<String> getMapLinksToListenTo() {
		synchronized (this.mapLinksToListenTo) {
			return new LinkedList<String>(mapLinksToListenTo);
		}
	}

	/**
	 * Whether the UpdateMonitor is currently listening.
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

	private void processDataAndAlertListeners(String JSon) {
		UpdateReceiverObject received = JsonUtils.mapJsonToType(JSon, UpdateReceiverObject.class);

		Map<String, Map<Integer, Map<?, ?>>> items = DataUtils.collapseUpdateMap(received.items);
		Map<String, Map<Integer, Map<?, ?>>> deletes = DataUtils.collapseUpdateMap(received.deletes);

		if (storeData) {
			dataMonitor.storeData(items, false);
			dataMonitor.storeData(deletes, true);
		} else {
			dataMonitor.storeVersions(items);
			dataMonitor.storeVersions(deletes);
		}

		alertListeners(items, deletes);
	}

	/**
	 * Remove a registered listener set to listen to all updates. The listener will no longer be informed of
	 * updates. If the listener is also listening to any MapLinks specifically, this listener will continue to
	 * listen to those specific MapLinks.
	 *
	 * @param listener The listener to remove.
	 */
	public void removeListener(UpdateListenerInterface listener) {
		if (generalListeners.contains(listener)) {
			generalListeners.remove(listener);
		}
	}

	/**
	 * Stop this listener from listening to the specified MapLink. If this is the last MapLink the listener
	 * was listening to, the listener will no longer be listening for specific MapLinks. If the listener is
	 * also listening to all MapLinks, it will continue to be informed of all updates.
	 * @param listener The listener to remove.
	 * @param mapLink The MapLink which this listener needn't listen to anymore.
	 */
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
	 * @param dataConnector The DataConnector to use.
	 */
	public void setDataConnector(DataConnector dataConnector) {
		this.dataConnector = dataConnector;
	}

	/**
	 * Set the DataMonitor to store data and version information.
	 * @param DataMonitor The DataMonitor to use.
	 */
	public void setDataMonitor(DataMonitor dataMonitor) {
		if (dataMonitor == null) {
			return;
		}
		this.dataMonitor = dataMonitor;
	}

	/**
	 * Set the maps which the UpdateMonitor should listen for explicitly. If this method is called while the
	 * UpdateMonitor is listening, it will complete its current request as if no change has taken place. When
	 * creating a new request, it will use only the MapLinks of the newly provided collection to create its
	 * request.
	 *
	 * @param mapLinksToListenTo The MapLinks to which the updateMonitor should listen.
	 */
	public void setMapLinksToListenTo(Collection<String> mapLinksToListenTo) {
		synchronized (this.mapLinksToListenTo) {
			if (mapLinksToListenTo.equals(this.mapLinksToListenTo)) {
				return;
			}
			this.mapLinksToListenTo.clear();
			if (mapLinksToListenTo != null) {
				this.mapLinksToListenTo.addAll(mapLinksToListenTo);
			}
		}
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
	 * Use the registered DataConnector to listen to updates and, when an update takes place, inform the
	 * listeners. Note that using this function will listen for ALL MapLinks known to this UpdateMonitor,
	 * which may take up to multiple seconds to process. To listen more specifically, use
	 * listenForUpdates(Collection&lt;String&gt; mapLinksToListenTo).
	 *
	 * @throws IllegalStateException When this UpdateMonitor is already listening for updates, the second
	 *             attempt at listening is ignored, and this exception is thrown.
	 * @throws NullPointerException When no DataConnector is registered, the attempt at listening is ignored,
	 *             and this exception is thrown.
	 * @throws HTTPException If the status code of the HTTP request is not 200, 204 or 400, this exception is
	 *             thrown and listening is halted.
	 */
	public void startListening() throws IllegalStateException, NullPointerException, HTTPException {
		startListening(mapLinksToListenTo);
	}

	/**
	 * Use the registered DataConnector to listen to updates and, when an update takes place, inform the
	 * listeners. This function blocks while listening. To stop listening, call stopListening() from another
	 * thread.
	 *
	 * @param mapLinksToListenTo A Collection indicating which MapLinks to listen for.
	 * @throws IllegalStateException When this UpdateMonitor is already listening for updates, the second
	 *             attempt at listening is ignored, and this exception is thrown.
	 * @throws NullPointerException When no DataConnector is registered, the attempt at listening is ignored,
	 *             and this exception is thrown.
	 * @throws HTTPException If the status code of the HTTP request is not 200, 204 or 400, this exception is
	 *             thrown and listening is halted.
	 */
	public void startListening(final Collection<String> mapLinksToListenTo) throws IllegalStateException,
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
		setMapLinksToListenTo(mapLinksToListenTo);

		HashMap<String, Integer> mapLinksForListening = new HashMap<String, Integer>();

		try {
			while (listening && !stopListening) {

				mapLinksForListening.clear();

				List<String> mapLinksOnServer = (List<String>) JsonUtils.mapJsonToList(dataConnector
						.getDataFromServerSession(StringUtils.EMPTY).getContent());

				synchronized (this.mapLinksToListenTo) {
					for (String mapLink : this.mapLinksToListenTo) {
						if (mapLinksOnServer.contains(mapLink)) {
							mapLinksForListening.put(mapLink, getDataMonitor().getVersion(mapLink));
						} else {
							Log.warning("Maplink " + mapLink + " does not exist on server");
						}
					}
					setMapLinksToListenTo(mapLinksForListening.keySet());
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
				} catch (ProcessingException e) {
					throw e;
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
							if (data.getContent().equals("CLIENT_RELEASED")) {
								throw new IllegalStateException(
										"The client has been released from the session.");
							}
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
	 * Stop the listening process. The UpdateMonitor will finish its current listening cycle and alert the
	 * listeners as needed, and then stop.
	 */
	public synchronized void stopListening() {
		stopListening = true;
		if (listening == false) {
			stopListening = false;
		}
	}
}
