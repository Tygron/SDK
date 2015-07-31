package com.tygron.pub.api.data.item;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tygron.pub.utils.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Stakeholder extends Item {

	private String name = StringUtils.EMPTY;
	private String shortName = StringUtils.EMPTY;
	private boolean playable = false;

	public String getName() {
		return name;
	}

	public String getShortName() {
		return shortName;
	}

	public boolean isPlayable() {
		return playable;
	}
}
