package com.tygron.pub.api.data.misc;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tygron.pub.utils.JsonUtils;
import com.tygron.pub.utils.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JoinableSessionObject {

	public static JoinableSessionObject getJoinableSessionObject(Map<String, ?> map) {
		return JsonUtils.castToObject(map, JoinableSessionObject.class);
	}

	private int id = StringUtils.NOTHING;
	private String name = StringUtils.EMPTY;
	private String language = StringUtils.EMPTY;
	private String sessionType = StringUtils.EMPTY;

	public int getID() {
		return id;
	}

	public String getLanguage() {
		return language;
	}

	public String getName() {
		return name;
	}

	public String sessionType() {
		return sessionType;
	}
}
