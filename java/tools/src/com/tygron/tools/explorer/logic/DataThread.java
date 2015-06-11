package com.tygron.tools.explorer.logic;

import java.rmi.UnexpectedException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.ws.http.HTTPException;
import com.tygron.pub.api.connector.DataPackage;
import com.tygron.pub.api.connector.ExtendedDataConnector;
import com.tygron.pub.api.data.UpdateMonitor;
import com.tygron.pub.api.data.misc.JoinableSessionObject;
import com.tygron.pub.api.data.misc.LocationObject;
import com.tygron.pub.api.enums.ClientType;
import com.tygron.pub.api.enums.GameMode;
import com.tygron.pub.api.enums.MapLink;
import com.tygron.pub.api.enums.events.ServerEvent;
import com.tygron.pub.api.listeners.UpdateListenerInterface;
import com.tygron.pub.exceptions.AuthenticationException;
import com.tygron.pub.logger.Log;
import com.tygron.pub.utils.DataUtils;
import com.tygron.pub.utils.JsonUtils;
import com.tygron.pub.utils.StringUtils;

public class DataThread extends Thread implements UpdateListenerInterface {

	public interface DataPaneListener {
		public void update();
	}

	private ExplorerCommunicator communicator = null;

	private final UpdateMonitor updateMonitor = new UpdateMonitor();

	private boolean initLoadComplete = false;
	private boolean connected = false;
	private boolean stopping = false;

	private final List<String> mapLinksToLoad = Arrays.asList(MapLink.stringValues());

	private final ExtendedDataConnector dc = new ExtendedDataConnector();

	public DataThread(ExplorerCommunicator communicator) {
		this.communicator = communicator;
	}

	public boolean attemptConnection(String server, String username, String password, String project,
			String slot) {
		// dc.setServerAddress(server);
		// dc.setUsernameAndPassword(username, password);
		boolean success = false;
		if (!StringUtils.isEmpty(slot)) {
			success = joinSession(slot);
		} else {
			success = startSession(project);
		}
		return success;
	}

	public List<JoinableSessionObject> attemptGetJoinableSessions() {
		try {

			return DataUtils.castToItemList(dc.getJoinableSessions(null, null, null),
					JoinableSessionObject.class);
		} catch (UnexpectedException e) {
			Log.exception(e, "Failed to retrieve joinable sessions.");
			return null;
		}
	}

	public Map<String, Collection<String>> attemptGetStartableProjects() {
		Map<String, Collection<String>> returnable = new HashMap<String, Collection<String>>();
		DataPackage data = dc.sendDataToServer(ServerEvent.GET_MENU_TREE, "NEW");

		Map<String, Map<String, Map<String, ?>>> fullMap = (Map<String, Map<String, Map<String, ?>>>) JsonUtils
				.mapJsonToMap(data.getContent());
		for (Entry<String, Map<String, ?>> project : fullMap.get("tree").entrySet()) {
			returnable.put(project.getKey(), project.getValue().keySet());
		}

		return returnable;
	}

	private void displayData() {
		communicator.setData(updateMonitor.getDataMonitor().getData());
	}

	private void displayDataUpdate() {
		communicator.setDataUpdate(updateMonitor.getDataMonitor().getData());
	}

	private void displayMap() {
		communicator.loadMap();
	}

	public Map<String, Map<Integer, Map<?, ?>>> getData() {
		return updateMonitor.getDataMonitor().getData();
	}

	public Map<Integer, Map<?, ?>> getData(MapLink mapLink) {
		return updateMonitor.getDataMonitor().getData(mapLink.toString());
	}

	public LocationObject getLocation() {
		DataPackage data = dc.getDataFromServerSessionLocation();
		LocationObject location = JsonUtils.mapJsonToType(data.getContent(), LocationObject.class);
		return location;
	}

	public boolean isConnected() {
		return this.initLoadComplete;
	}

	public boolean isLoaded() {
		return this.connected;
	}

	private boolean joinSession(String slot) {
		try {
			setStatus("Joining session...");
			boolean success = dc.joinSession(null, null, null, ClientType.VIEWER.toString(), slot,
					"No address", "Experimental Application (Explorer)");
			if (success) {
				connected = true;
				setStatus("Connection complete");
			}
			return success;
		} catch (AuthenticationException e) {
			throw e;
		} catch (UnexpectedException e) {
			setStatus("Failed, an exception occured");
			return false;
		}
	}

	private void loadData() {
		startLoadingData();
		startUpdateMonitoring();
	}

	private void printConnectionDetails() {
		Log.info("Serverslot: " + dc.getServerSlot());
		Log.info("Servertoken: " + dc.getServerToken());
		Log.info("Browser: " + dc.getServerAddress() + "slots/" + dc.getServerSlot() + "/?format=HTML&token="
				+ dc.getServerToken());
	}

	@Override
	public void run() {
		while (!(stopping || connected)) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				break;
			}
		}
		if (stopping) {
			return;
		}
		printConnectionDetails();
		displayMap();
		loadData();
		initLoadComplete = true;
		displayData();
	}

	public void setCredentials(String server, String username, String password) {
		dc.setServerAddress(server);
		dc.setUsernameAndPassword(username, password);

	}

	public void setLogin(String username, String password) {

	}

	private void setStatus(String status) {
		communicator.setStatus(status);
	}

	private void startLoadingData() {
		setStatus("Begin loading data");
		for (String mapLink : mapLinksToLoad) {
			setStatus("Loading " + mapLink);

			DataPackage data = dc.getDataFromServerSession(mapLink);
			if (data.getStatusCode() == 500) {
				continue;
			}
			String dataString = data.getContent();
			try {
				List<Map<String, Map<?, ?>>> dataList = (List<Map<String, Map<?, ?>>>) JsonUtils
						.mapJsonToList(dataString);
				updateMonitor.getDataMonitor().storeData(mapLink, DataUtils.dataListToMap(dataList), false);
			} catch (ClassCastException e) {
				setStatus("Failed to load " + mapLink + " because it failed to cast to a list");
			} catch (IllegalArgumentException e) {
				setStatus("Failed to load " + mapLink
						+ " because of an illegal argument. It's likely loading was aborted.");
			}
			setStatus("Loaded " + mapLink);
		}
		setStatus("Loading data completed");
		communicator.issueUpdate();
	}

	private boolean startSession(String project) {
		try {
			setStatus("Loading session...");
			boolean success = dc.startSessionAndJoin(null, null, null, project, "EN",
					GameMode.SINGLE_PLAYER.toString(), ClientType.VIEWER.toString(), "No address",
					"Experimental Application (Explorer)");
			if (success) {
				connected = true;
				setStatus("Connection complete");
			}
			return success;
		} catch (AuthenticationException e) {
			throw e;
		} catch (UnexpectedException e) {
			setStatus("Failed, an exception occured");
			// e.printStackTrace();
			return false;
		}
	}

	private void startUpdateMonitoring() {
		setStatus("Begin listening for updates");
		updateMonitor.setDataConnector(dc);
		updateMonitor.addListener(this);
		Thread updateMonitoringThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (!stopping) {
					try {
						updateMonitor.startListening(mapLinksToLoad);
					} catch (HTTPException e) {
						Log.exception(
								e,
								"HTTP Exception while listening to updates, because of status code: "
										+ e.getStatusCode());
					}
				}
			}
		});

		updateMonitoringThread.setDaemon(true);
		updateMonitoringThread.start();
	}

	public void stopThread() {
		stopping = true;
		updateMonitor.stopListening();
		if (dc.getServerSlot() != null) {
			Log.info(dc.closeConnectedSession() ? "Session ended" : "Failed to kill session");
		}
	}

	@Override
	public void update(Map<String, Map<Integer, Map<?, ?>>> items,
			Map<String, Map<Integer, Map<?, ?>>> deletes) {
		displayDataUpdate();
		// updateMonitor.stopListening();
		// updateMonitor.removeListener(this);
	}

}
