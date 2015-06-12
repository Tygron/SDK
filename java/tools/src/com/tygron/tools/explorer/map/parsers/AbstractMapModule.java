package com.tygron.tools.explorer.map.parsers;

import com.tygron.tools.explorer.gui.MapPane;
import com.tygron.tools.explorer.gui.MapPaneSubPane;
import com.tygron.tools.explorer.map.MapRenderManager;

public abstract class AbstractMapModule {

	private MapRenderManager renderManager;
	private MapPane mapPane;

	public final MapPane getMapPane() {
		return mapPane;
	}

	public abstract String getName();

	public abstract MapPaneSubPane getPane();

	public final MapRenderManager getRenderManager() {
		return this.renderManager;
	}

	public void load() {

	}

	public final void setMapPane(MapPane mapPane) {
		this.mapPane = mapPane;
	}

	public final void setRenderManager(MapRenderManager renderManager) {
		this.renderManager = renderManager;
	}

	protected void setStatus(String state) {
		mapPane.setStatus(state);
	}

	@Override
	public final String toString() {
		return getName();
	}

	public void unload() {

	}
}
