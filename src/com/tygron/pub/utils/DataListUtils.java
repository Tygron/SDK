package com.tygron.pub.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataListUtils {

	public static List<Object> collapseMapInList(List<Object> list) {
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
			for (Object subChild : firstChild.values()) {
				returnable.add(subChild);
			}
		}
		return returnable;
	}

}
