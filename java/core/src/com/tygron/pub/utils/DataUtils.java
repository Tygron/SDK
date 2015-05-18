package com.tygron.pub.utils;

import java.util.ArrayList;
import java.util.HashMap;
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

	/**
	 * For use when retrieving data sets directly from a session.
	 * @param list The list to collapse.
	 * @return The collapsed list.
	 */
	@Deprecated
	public static List<Object> collapseMapsInList(List<? extends Object> list) {
		if (list == null) {
			return null;
		}
		List<Object> returnable = new ArrayList<Object>();

		for (Object o : list) {
			if (!(o instanceof Map)) {
				returnable.add(0);
				continue;
			}

			Map firstChild = (Map) o;
			if (firstChild.size() != 1) {
				returnable.add(firstChild);
				continue;
			}

			// If there's only one child in the currently selected map in the list
			for (Object subChild : firstChild.values()) {
				returnable.add(subChild);
			}
		}
		return returnable;
	}

	/**
	 * For use when data sets are pulled via update poll.
	 * @param map the map to collapse.
	 * @return The collapsed maps.
	 */
	@Deprecated
	public static Map<Object, Object> collapseMapsInListsInMapsInMap(
			Map<? extends Object, ? extends Object> map) {
		if (map == null) {
			return null;
		}
		// input = items
		Map<Object, Object> returnable = new HashMap<Object, Object>();

		for (Entry e : map.entrySet()) {
			// entry = item : itembucket (which is a map).
			if (!(e.getValue() instanceof Map)) {
				returnable.put(e.getKey(), e.getValue());
				continue;
			}
			Map firstChild = (Map) e.getValue();

			// firstchild=itembucket (which is a map).
			if (firstChild.size() != 1) {
				returnable.put(e.getKey(), e.getValue());
				continue;
			}

			// If there's only one child in the currently selected bucket (which is likely list).
			returnable.put(e.getKey(), new ArrayList<Object>()); // The item's bucket is now a list.
			for (Object subChild : firstChild.values()) { // Subchild is the original bucket's content.
				if (!(subChild instanceof List)) {
					returnable.put(e.getKey(), subChild); // Not a list, so place it directly in the bucket.
					continue;
				}
				returnable.put(e.getKey(), collapseMapsInList((List) subChild)); // It is a list, so collapse
																					// it further.
			}
		}

		return returnable;
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
							Integer itemID = -1;
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

	/**
	 * Collapse a list returned by the data list URLs in the API into a standard format.
	 * @param map The map list data as returned by the data list URLs of the API, of the format
	 *            List&lt;Map&lt;String, Map&lt;?, ?&gt&gt&gt.
	 * @return Map&lt;String, Map&lt;Integer, Map&lt;?, ?&gt;&gt;&gt;, where the String key is a MapLink, the
	 *         Integer is an itemID and the niclosed map represent a single item of data.
	 */

	public static Map<Integer, Map<?, ?>> DataListToMap(List<Map<String, Map<?, ?>>> list) {
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
					Integer itemID = -1;
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
