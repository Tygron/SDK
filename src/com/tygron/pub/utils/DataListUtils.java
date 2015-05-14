package com.tygron.pub.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class was created to compensate for oddities originally present in the structure of "ItemMaps", the
 * lists of data associated with a specific project or session. These functions "collapse" parts of the
 * ItemMaps which are structurally redundant (e.g. maps with always only one key and another map as its
 * value).
 * @author Rudolf
 * 
 */
public class DataListUtils {

	/**
	 * For use when retrieving data sets directly from a session.
	 * @param list The list to collapse.
	 * @return The collapsed list.
	 */
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
}
