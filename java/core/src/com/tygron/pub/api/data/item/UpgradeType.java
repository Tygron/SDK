package com.tygron.pub.api.data.item;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tygron.pub.utils.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpgradeType extends Item {

	private String description = StringUtils.EMPTY;
	private String name = StringUtils.EMPTY;

	private List<Map<String, Integer>> pairs = new LinkedList<Map<String, Integer>>();
	private Map<Integer, Integer> collapsedPairs = null;

	public String getDescription() {
		return description;
	}

	public String getName() {
		return name;
	}

	public Map<Integer, Integer> getUpgradePairs() {
		if (collapsedPairs != null) {
			return collapsedPairs;
		}
		HashMap<Integer, Integer> returnable = new HashMap<Integer, Integer>();
		for (Map<String, Integer> pair : pairs) {
			returnable.put(pair.get("sourceFunctionID"), pair.get("targetFunctionID"));
		}
		collapsedPairs = returnable;
		return returnable;
	}
}
