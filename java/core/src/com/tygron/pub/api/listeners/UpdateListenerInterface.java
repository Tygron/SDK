package com.tygron.pub.api.listeners;

import java.util.Map;

public interface UpdateListenerInterface {

	/**
	 * This function is called whenever this listener is informed of an update by the UpdateMonitor. Provided
	 * to the listener are two maps containing all added, updated, and deleted items. They are ordered such
	 * that the keys are the MapLinks, and the values are a second layer of maps. The maps on the second layer
	 * are keyed by Id of the item, and the values are the maps of the items themselves. The maps provided to
	 * the listener contain only those MapLinks this listener is subscribed to. They do not neccesarily
	 * contain all Maplinks the lister is subscribed to. The maps are never null, though in theory the maps
	 * can be empty, and the items can be null.
	 * @param items The items that have been newly added or updated.
	 * @param deletes The items that have been deleted.
	 */
	public void update(Map<String, Map<Integer, Map<?, ?>>> items,
			Map<String, Map<Integer, Map<?, ?>>> deletes);

}
