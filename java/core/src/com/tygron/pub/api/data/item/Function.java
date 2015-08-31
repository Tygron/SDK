package com.tygron.pub.api.data.item;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tygron.pub.logger.Log;
import com.tygron.pub.utils.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Function extends AbstractFunctionBase {
	private String placementType = StringUtils.EMPTY;
	private Integer dimension = StringUtils.NOTHING;
	private Boolean deprecated = false;

	/**
	 * Get the total value of a categoryValue (such as cost per m²) per m² of this function.
	 * @param categoryValue The categoryValue to calculate
	 * @param override This function's override (may be null)
	 * @return The net value of the categoryValue per m² of this function
	 */
	public Double getCalculatedCategoryValue(CategoryValue categoryValue, FunctionOverride override) {
		double totalWeight = 0.0;
		double totalValue = 0.0;

		Map<FunctionCategory, Map<CategoryValue, Double>> categoryValues = getCategoryValues(override);

		for (FunctionCategory category : categoryValues.keySet()) {
			totalWeight += categoryValues.get(category).getOrDefault(CategoryValue.CATEGORY_WEIGHT,
					category.defaultValue(CategoryValue.CATEGORY_WEIGHT));
			try {
				totalValue += categoryValues.get(category).getOrDefault(CategoryValue.CATEGORY_WEIGHT,
						category.defaultValue(CategoryValue.CATEGORY_WEIGHT))
						* categoryValues.get(category).getOrDefault(categoryValue,
								category.defaultValue(categoryValue));
			} catch (NullPointerException e) {
				if (category.defaultValue(categoryValue) == null) {
					Log.warning("Null detected, because no default value is present: " + this.getName()
							+ " : " + category.toString() + " : " + categoryValue.toString());
					totalValue = 0;
					// throw new IllegalStateException();
				} else {
					throw e;
				}
			}
		}

		if (totalWeight != 0.0) {
			return totalValue / totalWeight;
		}
		return (double) StringUtils.NOTHING;
	}

	/**
	 * Get the categoryValue of this function for one specific category.
	 * @param category The category to retrieve the value for
	 * @param categoryValue The categoryValue to calculate
	 * @param override This function's override (may be null)
	 * @return The value of the categoryValue per m² of this category.
	 */
	public Double getCategoryValue(FunctionCategory category, CategoryValue categoryValue,
			FunctionOverride override) {
		if (override != null) {
			Double overrideCategoryValue = override.getCategoryValue(category, categoryValue);
			if (overrideCategoryValue != null) {
				return overrideCategoryValue;
			}
		}
		Double functionCategoryValue = override.getCategoryValue(category, categoryValue);
		return functionCategoryValue != null ? functionCategoryValue : category.defaultValue(categoryValue);
	}

	public Map<FunctionCategory, Map<CategoryValue, Double>> getCategoryValues(FunctionOverride override) {
		HashMap<FunctionCategory, Map<CategoryValue, Double>> returnable = new HashMap<FunctionCategory, Map<CategoryValue, Double>>();
		// All base values
		returnable.putAll(super.getCategoryValues());
		if (override != null) {
			for (FunctionCategory category : override.getCategoryValues().keySet()) {
				// Add each category also found in override as a new (empty) map, to prevent overwriting
				// original data
				Map<CategoryValue, Double> existingCategoryValues = returnable.get(category);
				returnable.put(category, new HashMap<CategoryValue, Double>());
				if (existingCategoryValues != null) {
					// If this category also existed in the original, place the values of the original back
					returnable.get(category).putAll(existingCategoryValues);
				}

				// Add each value in an override's category to the usable data.
				returnable.get(category).putAll(override.getCategoryValues().get(category));

				// At this point, for this category, all values in the returnable map are the original values,
				// unless overwritten.
			}
		}

		return returnable;
	}

	public String getDescription(FunctionOverride override) {
		if (override != null) {
			if (override.getDescription() != null) {
				return override.getDescription();
			}
		}
		return super.getDescription();
	}

	public int getDimension() {
		return dimension;
	}

	public int getFloorsDefault(FunctionOverride override) {
		if (override != null) {
			if (override.getFloorsDefault() != StringUtils.NOTHING) {
				return override.getFloorsDefault();
			}
		}
		return super.getFloorsDefault();
	}

	public int getFloorsMax(FunctionOverride override) {
		if (override != null) {
			if (override.getFloorsMax() != StringUtils.NOTHING) {
				return override.getFloorsMax();
			}
		}
		return super.getFloorsMax();
	}

	public int getFloorsMin(FunctionOverride override) {
		if (override != null) {
			if (override.getFloorsMin() != StringUtils.NOTHING) {
				return override.getFloorsMin();
			}
		}
		return super.getFloorsMin();
	}

	public Double getFunctionValue(FunctionValue functionValue, FunctionOverride override) {
		if (override != null) {
			if (override.getFloorsMin() != StringUtils.NOTHING) {
				return override.getFunctionValue(functionValue);
			}
		}
		return super.getFunctionValue(functionValue);
	}

	public Map<FunctionValue, Double> getFunctionValues(FunctionOverride override) {
		HashMap<FunctionValue, Double> returnable = new HashMap<FunctionValue, Double>();
		if (override != null) {
			returnable.putAll(override.getFunctionValues());
		}
		returnable.putAll(super.getFunctionValues());

		return returnable;
	}

	public String getName(FunctionOverride override) {
		if (override != null) {
			if (!StringUtils.isEmpty(override.getName())) {
				return override.getName();
			}
		}
		return super.getName();
	}

	public boolean isDeprecated() {
		return deprecated;
	}

	public boolean isFixedSize() {
		return dimension != StringUtils.NOTHING;
	}

	public boolean isFunctionCategory(FunctionCategory category, FunctionOverride override) {
		if (override != null) {
			Double categoryWeight = override.getCategoryValue(category, CategoryValue.CATEGORY_WEIGHT);
			if (categoryWeight != null) {
				return true;
			}
		}
		Double categoryWeight = getCategoryValue(category, CategoryValue.CATEGORY_WEIGHT);
		return categoryWeight != null;
	}

	public String placementType() {
		return this.placementType;
	}

}
