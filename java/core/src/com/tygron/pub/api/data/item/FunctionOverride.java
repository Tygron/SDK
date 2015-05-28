package com.tygron.pub.api.data.item;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tygron.pub.api.data.item.Function.FunctionValue;
import com.tygron.pub.utils.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FunctionOverride extends Item {

	private String description = null;
	private String name = null;
	private Map<FunctionValue, Double> functionValues = new HashMap<FunctionValue, Double>();

	// private Map<?, ?> categoryValues;

	public String getDescription() {
		return this.description;
	}

	public int getFloorsDefault() {
		return functionValues.getOrDefault(FunctionValue.DEFAULT_FLOORS, new Double(StringUtils.NOTHING))
				.intValue();
	}

	public int getFloorsMax() {
		return functionValues.getOrDefault(FunctionValue.MAX_FLOORS, new Double(StringUtils.NOTHING))
				.intValue();
	}

	public int getFloorsMin() {
		return functionValues.getOrDefault(FunctionValue.MIN_FLOORS, new Double(StringUtils.NOTHING))
				.intValue();
	}

	public String getName() {
		return this.name;
	}
}
