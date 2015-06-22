package com.tygron.pub.api.data.item;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FunctionOverride extends AbstractFunctionBase {

	private List<Map<String, ?>> indicatorScores = new LinkedList<Map<String, ?>>();

	@Override
	public String getDescription() {
		return super.getName();
	}

	@Override
	public int getFloorsDefault() {
		return super.getFloorsDefault();
	}

	@Override
	public int getFloorsMax() {
		return super.getFloorsMax();
	}

	@Override
	public int getFloorsMin() {
		return super.getFloorsMin();
	}

	@Override
	public Double getFunctionValue(FunctionValue functionValue) {
		return getFunctionValue(functionValue);
	}

	@Override
	public Map<FunctionValue, Double> getFunctionValues() {
		return getFunctionValues();
	}

	public double getIndicatorScore(Integer indicatorID) {
		for (Map<String, ?> indicatorScore : indicatorScores) {
			if (indicatorScore.get("indicatorID").equals(indicatorID)) {
				return (double) indicatorScore.get("score");
			}
		}
		return 0.0;
	}

	@Override
	public String getName() {
		return super.getName();
	}

	public boolean hasIndicatorScore(Integer indicatorID) {
		for (Map<String, ?> indicatorScore : indicatorScores) {
			if (indicatorScore.get("indicatorID").equals(indicatorID)) {
				return true;
			}
		}
		return false;
	}
}
