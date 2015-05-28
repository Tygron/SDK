package com.tygron.pub.api.data.item;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tygron.pub.utils.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Function extends Item {

	public static enum FunctionValue {
			MIN_FLOORS,
			MAX_FLOORS,
			DEFAULT_FLOORS,
			ENVIRONMENT_EFFECT;
	}

	private String description = StringUtils.EMPTY;
	private String name = StringUtils.EMPTY;
	private Map<FunctionValue, Double> functionValues = new HashMap<FunctionValue, Double>();
	// private Map<?, ?> categoryValues;
	private String placementType = StringUtils.EMPTY;

	public String getDescription(FunctionOverride override) {
		if (override != null) {
			if (override.getDescription() != null) {
				return override.getDescription();
			}
		}
		return this.description;
	}

	public int getFloorsDefault(FunctionOverride override) {
		if (override != null) {
			if (override.getFloorsDefault() != StringUtils.NOTHING) {
				return override.getFloorsDefault();
			}
		}
		return functionValues.getOrDefault(FunctionValue.DEFAULT_FLOORS, new Double(StringUtils.NOTHING))
				.intValue();
	}

	public int getFloorsMax(FunctionOverride override) {
		if (override != null) {
			if (override.getFloorsMax() != StringUtils.NOTHING) {
				return override.getFloorsMax();
			}
		}
		return functionValues.getOrDefault(FunctionValue.MAX_FLOORS, new Double(StringUtils.NOTHING))
				.intValue();
	}

	public int getFloorsMin(FunctionOverride override) {
		if (override != null) {
			if (override.getFloorsMin() != StringUtils.NOTHING) {
				return override.getFloorsMin();
			}
		}
		return functionValues.getOrDefault(FunctionValue.MIN_FLOORS, new Double(StringUtils.NOTHING))
				.intValue();
	}

	public String getName(FunctionOverride override) {
		if (override != null) {
			if (override.getName() != null) {
				return override.getName();
			}
		}
		return this.name;
	}

	public String placementType() {
		return this.placementType;
	}
}
