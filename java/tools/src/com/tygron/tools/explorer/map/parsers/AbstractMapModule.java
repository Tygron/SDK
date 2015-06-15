package com.tygron.tools.explorer.map.parsers;

import com.tygron.tools.explorer.gui.MapPaneSubPane;
import com.tygron.tools.explorer.logic.ExplorerCommunicator;
import com.tygron.tools.explorer.map.MapRenderManager;

public abstract class AbstractMapModule {

	private MapRenderManager renderManager;
	private ExplorerCommunicator communicator;

	public final ExplorerCommunicator getCommunicator() {
		return this.communicator;
	}

	public abstract String getName();

	public abstract MapPaneSubPane getPane();

	public final MapRenderManager getRenderManager() {
		return this.renderManager;
	}

	public boolean isFunctional() {
		return true;
	}

	public void load() {

	}

	public final void setCommunicator(ExplorerCommunicator communicator) {
		this.communicator = communicator;
	}

	public final void setRenderManager(MapRenderManager renderManager) {
		this.renderManager = renderManager;
	}

	protected void setStatus(String state) {
		communicator.setStatus(state);
	}

	@Override
	public final String toString() {
		return getName();
	}

	public void unload() {

	}
}
