package com.tygron.pub.api.data.item;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tygron.pub.utils.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AbstractFunctionBase extends Item {

	public static enum CategoryValue {
			CATEGORY_WEIGHT,
			UNIT_SIZE_M2,
			HEAT_FLOW_M2_YEAR,
			PARKING_LOTS_PER_M2,
			CONSTRUCTION_COST_M2,
			DEMOLISH_COST_M2,
			BUYOUT_COST_M2,
			SELL_PRICE_M2;
	}

	public static enum FunctionCategory {
			SOCIAL(
				1.0,// weight
				80.0,// size
				-0.30000001192092896,// heat
				-0.029999999329447746,// parking
				2000.0,// construction
				250.0,// demolish
				2400.0,// buyout
				3000.0), // sell
			NORMAL(
				1.0,// weight
				150.0,// size
				-0.30000001192092896,// heat
				-0.017000000923871994,// parking
				3000.0,// construction
				250.0,// demolish
				3200.0,// buyout
				4000.0), // sell
			LUXE(
				1.0,// weight
				150.0,// size
				-0.30000001192092896,// heat
				0.008999999612569809,// parking
				4000.0,// construction
				250.0,// demolish
				4000.0,// buyout
				5000.0), // sell
			ROAD(
				1.0,// weight
				1.0,// size
				0.0,// heat
				0.0,// parking
				100.0,// construction
				20.0,// demolish
				0.0,// buyout
				0.0), // sell
			PAVED_AREA, // Could not find default values
			EDUCATION(
				1.0,// weight
				1.0,// size
				-0.30000001192092896,// heat
				-0.05999999865889549,// parking
				2000.0,// construction
				250.0,// demolish
				0.0,// buyout
				0.0), // sell
			HEALTHCARE(
				1.0,// weight
				1.0,// size
				-0.30000001192092896,// heat
				-0.07999999821186066,// parking
				2000.0,// construction
				250.0,// demolish
				0.0,// buyout
				0.0), // sell
			PARK(
				1.0,// weight
				1.0,// size
				-0.0,// heat
				-0.0,// parking
				500.0,// construction
				10.0,// demolish
				0.0,// buyout
				0.0), // sell
			NATURE(
				1.0,// weight
				1.0,// size
				0.0,// heat
				0.0,// parking
				0.0,// construction
				10.0,// demolish
				0.0,// buyout
				0.0), // sell
			INDUSTRY(
				1.0,// weight
				1.0,// size
				-0.30000001192092896,// heat
				-0.019999999552965164,// parking
				4000.0,// construction
				250.0,// demolish
				4000.0,// buyout
				5000.0), // sell
			OFFICES(
				1.0,// weight
				1.0,// size
				-0.30000001192092896,// heat
				-0.019999999552965164,// parking
				4000.0,// construction
				250.0,// demolish
				4000.0,// buyout
				5000.0), // sell
			OTHER(
				1.0,// weight
				1.0,// size
				-0.30000001192092896,// heat
				-0.019999999552965164,// parking
				4000.0,// construction
				250.0,// demolish
				4000.0,// buyout
				5000.0), // sell
			SENIOR(
				// Could not find default values
				1.0,// weight
				100.0,// size
				-0.30000001192092896,// heat
				-0.014999999664723873,// parking
				2000.0,// construction
				250.0,// demolish
				2400.0,// buyout
				3000.0), // sell
			UNDERGROUND_WITH_FREE_TOP, // Could not find default values
			UNDERGROUND_WITH_TOP_BUILDING, // Could not find default values
			DIKE, // Could not find default values
			SHOPPING(
				1.0,// weight
				1.0,// size
				-0.30000001192092896,// heat
				-0.032999999821186066,// parking
				4000.0,// construction
				250.0,// demolish
				4000.0,// buyout
				5000.0), // sell,
			AGRICULTURE(
				1.0,// weight
				1.0,// size
				-0.30000001192092896,// heat
				0.0,// parking
				4000.0,// construction
				250.0,// demolish
				4000.0,// buyout
				5000.0), // sell
			LEISURE(
				1.0,// weight
				1.0,// size
				-0.30000001192092896,// heat
				0.013000000268220901,// parking
				4000.0,// construction
				500.0,// demolish
				4000.0,// buyout
				5000.0), // sell
			STUDENT(
				// Could not find default values
				1.0,// weight
				20.0,// size
				-0.30000001192092896,// heat
				-0.00800000037997961,// parking
				2000.0,// construction
				250.0,// demolish
				2400.0,// buyout
				3000.0), // sell,
			GARDEN(
				1.0,// weight
				1.0,// size
				0.0,// heat
				0.0,// parking
				500.0,// construction
				10.0,// demolish
				0.0,// buyout
				0.0) // sell
		;
		public static final FunctionCategory[] VALUES = FunctionCategory.values();
		private Map<CategoryValue, Double> defaultValues = new HashMap<CategoryValue, Double>();

		private FunctionCategory() {
		}

		private FunctionCategory(double weight, double size, double heat, double parking,
				double construction, double demolish, double buyout, double sell) {
			defaultValues.put(CategoryValue.CATEGORY_WEIGHT, weight);
			defaultValues.put(CategoryValue.UNIT_SIZE_M2, size);
			defaultValues.put(CategoryValue.HEAT_FLOW_M2_YEAR, heat);
			defaultValues.put(CategoryValue.PARKING_LOTS_PER_M2, parking);
			defaultValues.put(CategoryValue.CONSTRUCTION_COST_M2, construction);
			defaultValues.put(CategoryValue.DEMOLISH_COST_M2, demolish);
			defaultValues.put(CategoryValue.BUYOUT_COST_M2, buyout);
			defaultValues.put(CategoryValue.SELL_PRICE_M2, sell);
		}

		public Double defaultValue(CategoryValue categoryValue) {
			return defaultValues.getOrDefault(categoryValue, null);
		}
	}

	public static enum FunctionValue {
			ENVIRONMENT_EFFECT,
			HEAT_EFFECT,
			UNIT_SIZE_M2,
			HEAT_FLOW_M2_YEAR,
			SAFE_ZONE_DISTANCE_M,
			PARKING_LOTS_PER_M2,
			CONSTRUCTION_COST_M2,
			DEMOLISH_COST_M2,
			SELL_PRICE_M2,
			WATER_STORAGE_M2,
			GREEN_M2,
			CONSTRUCTION_TIME_IN_MONTHS,
			MIN_FLOORS,
			DEFAULT_FLOORS,
			MAX_FLOORS,
			DEFAULT_FLOOR_HEIGHT,
			DEMOLISH_TIME_IN_MONTHS,
			ZONING_PERMIT_REQUIRED,
			GROUND_FLOOR_HEIGHT,
			EXTRA_FLOOR_HEIGHT,
			TOP_FLOOR_HEIGHT,
			SLANTING_ROOF_HEIGHT,
			TRAFFIC_FLOW,
			TRAFFIC_SPEED,
			TRAFFIC_LANES;
	}

	private String description = StringUtils.EMPTY;
	private String name = StringUtils.EMPTY;
	private Map<FunctionValue, Double> functionValues = new HashMap<FunctionValue, Double>();
	private Map<FunctionCategory, Map<CategoryValue, Double>> categoryValues = new HashMap<FunctionCategory, Map<CategoryValue, Double>>();

	protected Double getCategoryValue(FunctionCategory category, CategoryValue categoryValue) {
		if (!getCategoryValues().containsKey(category)) {
			return null;
		}
		if (!getCategoryValues().get(category).containsKey(categoryValue)) {
			return null;
		}
		return getCategoryValues().get(category).get(categoryValue);
	}

	protected Map<FunctionCategory, Map<CategoryValue, Double>> getCategoryValues() {
		return categoryValues;
	}

	protected String getDescription() {
		return this.description;
	}

	protected int getFloorsDefault() {
		return functionValues.getOrDefault(FunctionValue.DEFAULT_FLOORS, new Double(StringUtils.NOTHING))
				.intValue();
	}

	protected int getFloorsMax() {
		return functionValues.getOrDefault(FunctionValue.MAX_FLOORS, new Double(StringUtils.NOTHING))
				.intValue();
	}

	protected int getFloorsMin() {
		return functionValues.getOrDefault(FunctionValue.MIN_FLOORS, new Double(StringUtils.NOTHING))
				.intValue();
	}

	protected Double getFunctionValue(FunctionValue functionValue) {
		return functionValues.get(functionValue);
	}

	protected Map<FunctionValue, Double> getFunctionValues() {
		return functionValues;
	}

	protected String getName() {
		return this.name;
	}
}
