package com.tygron.pub.api.data.item;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tygron.pub.utils.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Setting {

	private int id = StringUtils.NOTHING;
	private String value = StringUtils.EMPTY;

	public int getID() {
		return id;
	}

	public String getValue() {
		return value;
	}
}
