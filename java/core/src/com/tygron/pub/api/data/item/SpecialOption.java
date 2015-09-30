package com.tygron.pub.api.data.item;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tygron.pub.utils.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SpecialOption extends Item {

	private String name = StringUtils.EMPTY;
	private String description = StringUtils.EMPTY;
	private String imageName = StringUtils.EMPTY;

	public String getDescription() {
		return description;
	}

	public String getImageName() {
		return imageName;
	}

	public String getName() {
		return name;
	}
}
