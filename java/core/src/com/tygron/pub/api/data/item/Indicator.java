package com.tygron.pub.api.data.item;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tygron.pub.utils.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Indicator extends Item {

	private Map<Integer, Double[]> targets = null; // ordered by levelID
	private Double[] progress = null; // This does not exist for green/parking
	private Map<Integer, Double> startOfGameScoreValues = null; // ordered by levelID?

	private Map<String, Double> mapTypeValues = null; // current scores (Current or Maquette)
	private Map<String, Double> exactActorValues = null; // current absolute values (Current or Maquette)

	private String name = StringUtils.EMPTY;
	private String shortName = StringUtils.EMPTY;

	public double getActorValue(String mapType) {
		return exactActorValues.getOrDefault(mapType, (double) StringUtils.NOTHING);
	}

	public double getCurrentScore(String mapType) {
		return mapTypeValues.getOrDefault(mapType, (double) StringUtils.NOTHING);
	}

	public String getName() {
		return name;
	}

	public Double[] getProgress() {
		return progress;
	}

	public String getShortName() {
		return shortName;
	}

	public Double getStartOfGameScoreValues(int levelID) {
		return startOfGameScoreValues.getOrDefault(levelID, null);
	}

	public Double[] getTargets(int levelID) {
		return targets.getOrDefault(levelID, null);
	}
}
