package com.tygron.pub.api.data.item;

import java.util.Arrays;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tygron.pub.api.enums.MapType;
import com.tygron.pub.utils.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Indicator extends Item {

	private Map<Integer, Double[]> targets = null; // ordered by levelID
	private Double[] progress = null; // Progress is twice as long as targets arrays. The first series is in
										// the "current" map, the second in the "maquette" map. This does not
										// exist for green/parking.
	private Map<Integer, Double> startOfGameScoreValues = null; // ordered by levelID?

	private Map<MapType, Double> mapTypeValues = null; // current scores (Current or Maquette)
	private Map<MapType, Double> exactActorValues = null; // current absolute values (Current or Maquette)

	private String name = StringUtils.EMPTY;
	private String shortName = StringUtils.EMPTY;

	public double getActorValue(MapType mapType) {
		return exactActorValues.getOrDefault(mapType, (double) StringUtils.NOTHING);
	}

	public double getCurrentScore(MapType mapType) {
		return mapTypeValues.getOrDefault(mapType, (double) StringUtils.NOTHING);
	}

	public String getName() {
		return name;
	}

	public Double[] getProgress() {
		return progress;
	}

	public Double[] getProgress(MapType mapType) {
		switch (mapType) {
			case CURRENT:
				return Arrays.copyOfRange(progress, 0, progress.length / 2);
			case MAQUETTE:
				return Arrays.copyOfRange(progress, progress.length / 2, progress.length);
			default:
				throw new IllegalArgumentException("Progress parameters invalid");
		}
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
