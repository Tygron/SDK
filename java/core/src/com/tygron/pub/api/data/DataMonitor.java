package com.tygron.pub.api.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import com.tygron.pub.api.enums.MapLink;
import com.tygron.pub.logger.Log;

public class DataMonitor {

	private final Map<String, Integer> versions = new HashMap<String, Integer>();
	private final Map<String, Map<Integer, Map<?, ?>>> data = new ConcurrentHashMap<String, Map<Integer, Map<?, ?>>>();

	{
		for (String s : MapLink.stringValues()) {
			versions.put(s, -1);
		}
	}

	public Map<String, Map<Integer, Map<?, ?>>> getData() {
		return data;
	}

	public Map<Integer, Map<?, ?>> getData(String mapLink) {
		return data.get(mapLink);
	}

	public Integer getVersion(String mapLink) {
		if (versions.get(mapLink) == null) {
			versions.put(mapLink, -1);
		}
		return versions.get(mapLink);
	}

	public void setVersion(String mapLink, int version) {
		versions.put(mapLink, version);
	}

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

	public void storeData(String mapLink, Map<Integer, Map<?, ?>> map, boolean delete) {
		for (Entry<Integer, Map<?, ?>> entry : map.entrySet()) {
			storeData(mapLink, entry.getKey(), entry.getValue(), delete);
		}
	}

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

	public void updateVersion(String mapLink, int version) {
		versions.put(mapLink, Math.max(getVersion(mapLink), version));
	}

}
