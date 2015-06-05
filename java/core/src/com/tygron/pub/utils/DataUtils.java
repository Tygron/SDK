package com.tygron.pub.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.tygron.pub.logger.Log;

/**
 * This class was created to compensate for oddities originally present in the structure of "ItemMaps", the
 * lists of data associated with a specific project or session. These functions "collapse" parts of the
 * ItemMaps which are structurally redundant (e.g. maps with always only one key and another map as its
 * value).
 * @author Rudolf
 *
 */
public class DataUtils {

	public static <T> List<T> castToItemList(List<Map<?, ?>> list, Class<T> target) {
		List<T> returnable = new LinkedList<T>();
		for (Map<?, ?> map : list) {
			returnable.add(DataUtils.castToItemObject(map, target));
		}
		return returnable;
	}

	public static <T> Map<Integer, T> castToItemMap(Map<Integer, Map<?, ?>> map, Class<T> target) {
		Map<Integer, T> returnable = new HashMap<Integer, T>();
		for (Entry<Integer, Map<?, ?>> item : map.entrySet()) {
			returnable.put(item.getKey(), castToItemObject(item.getValue(), target));
		}
		return returnable;
	}

	public static <T> T castToItemObject(Map<?, ?> map, Class<T> target) {
		// We can do this via reflection, or take a shortcut using the json utilities.
		String json = JsonUtils.mapObjectToJson(map);
		return JsonUtils.mapJsonToType(json, target);
	}

	/**
	 * Collapse a map returned by the long-polling update mechanic of the API into a standard format.
	 * @param map The map of data as returned by the update mechanic of the API, of the format Map&lt;String,
	 *            Map&lt;String, List&lt;Map&lt;String, Map&lt;?, ?&gt;&gt;&gt;&gt;&gt;.
	 * @return Map&lt;String, Map&lt;Integer, Map&lt;?, ?&gt;&gt;&gt;, where the String key is a MapLink, the
	 *         Integer is an itemID and the niclosed map represent a single item of data.
	 */
	public static Map<String, Map<Integer, Map<?, ?>>> collapseUpdateMap(
			Map<String, Map<String, List<Map<String, Map<?, ?>>>>> map) {
		if (map == null) {
			return null;
		}

		Map<String, Map<Integer, Map<?, ?>>> returnable = new HashMap<String, Map<Integer, Map<?, ?>>>();

		try {
			for (Entry<String, Map<String, List<Map<String, Map<?, ?>>>>> entry : map.entrySet()) {
				HashMap<Integer, Map<?, ?>> returnableItemMap = new HashMap<Integer, Map<?, ?>>();

				returnable.put(entry.getKey(), returnableItemMap);

				Map<String, List<Map<String, Map<?, ?>>>> firstChild = entry.getValue();

				if (firstChild.size() > 1) {
					throw new IndexOutOfBoundsException();
				}

				for (List<Map<String, Map<?, ?>>> secondChildObject : firstChild.values()) {
					List<Map<String, Map<?, ?>>> secondChild = secondChildObject;
					for (Map<String, Map<?, ?>> thirdChild : secondChild) {
						if (thirdChild.size() > 1) {
							throw new IndexOutOfBoundsException();
						}
						for (Map<?, ?> item : thirdChild.values()) {
							Integer itemID = StringUtils.NOTHING;
							try {
								itemID = (Integer) item.get("id");
							} catch (NumberFormatException e) {
								String failString = "Failed to parse id of item"
										+ (item.toString().length() > 200 ? ": " + item.toString()
												: ", to large to print.");
								Log.exception(e, failString);
								break;
							}
							returnableItemMap.put(itemID, item);
						}
					}
				}

			}
		} catch (ClassCastException e) {
			Log.exception(e, "Failed to collapse map, because a collection was not of the expected type.");
		}

		return returnable;
	}

	public static <T> Map<Integer, T> dataListToItemMap(List<Map<String, Map<?, ?>>> list, Class<T> target) {
		Map<Integer, Map<?, ?>> map = dataListToMap(list);
		if (list == null) {
			return null;
		}
		return castToItemMap(map, target);
	}

	/**
	 * Collapse a list returned by the data list URLs in the API into a standard format.
	 * @param map The map list data as returned by the data list URLs of the API, of the format
	 *            List&lt;Map&lt;String, Map&lt;?, ?&gt&gt&gt.
	 * @return Map&lt;String, Map&lt;Integer, Map&lt;?, ?&gt;&gt;&gt;, where the String key is a MapLink, the
	 *         Integer is an itemID and the niclosed map represent a single item of data.
	 */
	public static Map<Integer, Map<?, ?>> dataListToMap(List<Map<String, Map<?, ?>>> list) {
		if (list == null) {
			return null;
		}

		Map<Integer, Map<?, ?>> returnableItemMap = new HashMap<Integer, Map<?, ?>>();

		try {
			for (Map<String, Map<?, ?>> firstChild : list) {
				if (firstChild.size() > 1) {
					throw new IndexOutOfBoundsException();
				}

				for (Map<?, ?> item : firstChild.values()) {
					Integer itemID = StringUtils.NOTHING;
					try {
						itemID = (Integer) item.get("id");
					} catch (NumberFormatException e) {
						String failString = "Failed to parse id of item"
								+ (item.toString().length() > 200 ? ": " + item.toString()
										: ", to large to print.");
						Log.exception(e, failString);
						break;
					}
					returnableItemMap.put(itemID, item);
				}
			}
		} catch (ClassCastException e) {
			Log.exception(e, "Failed to collapse map, because a collection was not of the expected type.");
		}

		return returnableItemMap;
	}
}
