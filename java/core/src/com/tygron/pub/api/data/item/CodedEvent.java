package com.tygron.pub.api.data.item;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CodedEvent extends Item {
	List<Object> parameters = new LinkedList<Object>();

	public List<Object> getParameters() {
		return new ArrayList<Object>(parameters);
	}

	public String getSimpleEventType() {
		for (String eventType : ((Map<String, String>) parameters.get(0)).values()) {
			return eventType;
		}
		return null;
	}
}
