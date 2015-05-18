package com.tygron.pub.api.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import com.tygron.pub.api.enums.MapLink;
import com.tygron.pub.logger.Log;

/**
 * The DataMonitor is used as a means of storing data from a running session. It is automatically created when
 * an UpdateMonitor is created, but this default implementation can also be overridden.
 * @author Rudolf
 *
 */
public class DataMonitor {

	private final Map<String, Integer> versions = new HashMap<String, Integer>();
	private final Map<String, Map<Integer, Map<?, ?>>> data = new ConcurrentHashMap<String, Map<Integer, Map<?, ?>>>();

	{
		for (String s : MapLink.stringValues()) {
			versions.put(s, -1);
		}
	}

	/**
	 * Get all data currently stored in the DataMonitor.
	 * @return The map containing all the Data.
	 */
	public Map<String, Map<Integer, Map<?, ?>>> getData() {
		return data;
	}

	/**
	 * Get the data of a specific MapLink.
	 * @param mapLink The type of data to return.
	 * @return The map containing the specified data.
	 */
	public Map<Integer, Map<?, ?>> getData(String mapLink) {
		return data.get(mapLink);
	}

	/**
	 * Get the most recent version of the specified MapLink.
	 * @param mapLink The MapLink of which to retrieve the current version.
	 * @return The current version.
	 */
	public Integer getVersion(String mapLink) {
		if (versions.get(mapLink) == null) {
			versions.put(mapLink, -1);
		}
		return versions.get(mapLink);
	}

	/**
	 * Set the current version for a specific MapLink. This method should not be used under normal
	 * circumstances, as this method allows the version to regress. To only allow for the version to increase,
	 * use updateVersion(String mapLink, int version).
	 * @param mapLink The MapLink of which to set the version.
	 * @param version The version to set the MapLink to.
	 */
	public void setVersion(String mapLink, int version) {
		versions.put(mapLink, version);
	}

	/**
	 * Store data in the DataMonitor. All items in the map are individually copied into the map containing the
	 * data. If the delete parameter is set to true, the data is deleted from the DataMonitor's map based on
	 * MapLink and id rather than storing. Regardless of whether it's storing of deleting, versions are also
	 * automatically updated.
	 * @param map The map containing the data to store.
	 * @param delete Whether to insert the data, or to delete it.
	 */
	public void storeData(Map<String, Map<Integer, Map<?, ?>>> map, boolean delete) {
		for (String mapLink : map.keySet()) {
			storeData(mapLink, map.get(mapLink), delete);
		}
	}

	private void storeData(String mapLink, Integer id, Map<?, ?> item, boolean delete) {
		if (mapLink == null || id == null) {
			Log.warning("Failed to store data because the following was null:"
					+ (mapLink == null ? " Maplink" : "") + (id == null ? " id" : ""));
			return;
		}

		Map<Integer, Map<?, ?>> itemMap = data.get(mapLink);
		if (itemMap == null) {
			itemMap = new HashMap<Integer, Map<?, ?>>();
			data.put(mapLink, itemMap);
		}
		if (delete) {
			itemMap.remove(id);
		} else {
			itemMap.put(id, item);
		}

		try {
			updateVersion(mapLink, (Integer) item.get("version"));
		} catch (ClassCastException e) {
			Log.exception(e, mapLink + " " + id + "' version is not of type Integer.");
		}
	}

	/**
	 * Store data in the DataMonitor. All items in the map are individually copied into the map containing the
	 * data. If the delete parameter is set to true, the data is deleted from the DataMonitor's map based on
	 * MapLink and id rather than storing. Regardless of whether it's storing of deleting, versions are also
	 * automatically updated.
	 * @param mapLink The type of data to store.
	 * @param map The map containing the data to store.
	 * @param delete Whether to insert the data, or to delete it.
	 */
	public void storeData(String mapLink, Map<Integer, Map<?, ?>> map, boolean delete) {
		for (Entry<Integer, Map<?, ?>> entry : map.entrySet()) {
			storeData(mapLink, entry.getKey(), entry.getValue(), delete);
		}
	}

	/**
	 * Store the version of data in the DataMonitor.
	 * @param map The data of which the version must be updated.
	 */
	public void storeVersions(Map<String, Map<Integer, Map<?, ?>>> map) {
		for (String mapLink : map.keySet()) {
			for (Entry<Integer, Map<?, ?>> entry : map.get(mapLink).entrySet()) {

				try {
					updateVersion(mapLink, (Integer) entry.getValue().get("version"));
				} catch (ClassCastException e) {
					Log.exception(e, mapLink + " " + entry.getKey() + "' version is not of type Integer.");
				}
			}
		}
	}

	/**
	 * Update the current version for a specific MapLink. This method should be used in preference of
	 * setVersion(String mapLink, int version), as this method does not allow the version to regress.
	 * @param mapLink The MapLink of which to set the version.
	 * @param version The version to update the MapLink to.
	 */
	public void updateVersion(String mapLink, int version) {
		versions.put(mapLink, Math.max(getVersion(mapLink), version));
	}

}
