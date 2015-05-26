package com.tygron.pub.api.data.item;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tygron.pub.utils.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Setting extends Item {

	private String value = StringUtils.EMPTY;

	public String getValue() {
		return value;
	}
}
