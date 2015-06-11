package com.tygron.tools.explorer.logic;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import com.tygron.pub.api.data.misc.JoinableSessionObject;
import com.tygron.pub.api.data.misc.LocationObject;
import com.tygron.pub.api.enums.MapLink;
import com.tygron.pub.logger.Log;
import com.tygron.tools.explorer.gui.DataPane;
import com.tygron.tools.explorer.gui.GameExplorerSubPane;
import com.tygron.tools.explorer.gui.LoginPane;
import com.tygron.tools.explorer.gui.MapPane;
import com.tygron.tools.explorer.gui.StatusPane;

public class ExplorerCommunicator {

	private LoginPane loginPane = null;
	private StatusPane statusPane = null;
	private DataPane dataPane = null;
	private MapPane mapPane = null;

	private List<GameExplorerSubPane> panes = new LinkedList<GameExplorerSubPane>();

	private final DataThread dataThread = new DataThread(this);

	public boolean attemptConnection(String server, String username, String password, String project,
			String slot) {
		return dataThread.attemptConnection(server, username, password, project, slot);

	}

	public Map<Integer, Map<?, ?>> getData(MapLink mapLink) {
		return dataThread.getData(mapLink);
	}

	public List<JoinableSessionObject> getJoinableProjects() {
		return dataThread.attemptGetJoinableSessions();
	}

	public LocationObject getLocation() {
		if (dataThread == null) {
			return null;
		}

		LocationObject location = dataThread.getLocation();

		return location;
	}

	public Map<String, Collection<String>> getStartableProjects() {
		return dataThread.attemptGetStartableProjects();
	}

	public void issueUpdate() {
		for (GameExplorerSubPane subPane : panes) {
			try {
				subPane.processUpdate();
			} catch (Exception e) {
				Log.exception(e, "Uncaught exception while issuing update "
						+ (subPane == null ? "to null pane." : "to " + subPane.getClass() + "."));
			}
		}
	}

	public void loadMap() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (mapPane == null) {
					return;
				}
				mapPane.loadMap();
			}
		});
	}

	public void registerPane(GameExplorerSubPane subPane) {
		if (subPane instanceof StatusPane) {
			statusPane = (StatusPane) subPane;
		}
		if (subPane instanceof DataPane) {
			dataPane = (DataPane) subPane;
		}
		if (subPane instanceof MapPane) {
			mapPane = (MapPane) subPane;
		}
		if (subPane instanceof LoginPane) {
			loginPane = (LoginPane) subPane;
		}
		panes.add(subPane);
	}

	/**
	 * The selection of data on the dataPane, to be used in, for example, the mapPane
	 */
	public void selectData(final String mapLink, final Object data) {
		if (mapPane == null) {
			return;
		}
		mapPane.updateDisplayedData(mapLink, data);
	}

	/**
	 * The selection of data on the dataPane, to be used in, for example, the mapPane
	 */
	public void selectDataByUpdate(final String mapLink, final Object data) {
		if (mapPane == null) {
			return;
		}
		mapPane.updateDisplayedData(mapLink, data);
	}

	public void setCredentials(String server, String username, String password) {
		dataThread.setCredentials(server, username, password);
	}

	/**
	 * The first display of data for the dataPane
	 */
	public void setData(final Map<String, Map<Integer, Map<?, ?>>> data) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (dataPane == null) {
					return;
				}
				dataPane.displayData(data);
			}
		});
	}

	/**
	 * The subsequent displays of data for the dataPane
	 */
	public void setDataUpdate(final Map<String, Map<Integer, Map<?, ?>>> data) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (dataPane == null) {
					return;
				}
				dataPane.displayDataUpdate(data);
			}
		});
	}

	public void setStatus(String status) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (statusPane == null) {
					return;
				}
				statusPane.setInnerStatus(status);
			}
		});
	}

	public void startDataThread() {
		new Thread(dataThread).start();
	}

	public void stopDataThread() {
		dataThread.stopThread();
	}
}
