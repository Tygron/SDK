package com.tygron.pub.api.data.misc;

import java.util.HashMap;
import java.util.Map;
import com.tygron.pub.utils.StringUtils;

public class LocationObject {

	public String format = StringUtils.EMPTY;
	public Map<String, Double> envelope = new HashMap<String, Double>();
	public Map<String, Double> centerPoint = new HashMap<String, Double>();

	public Double getCenterX() {
		return centerPoint.get("x");
	}

	public Double getCenterY() {
		return centerPoint.get("y");
	}

	public Double getCenterZ() {
		return centerPoint.get("z");
	}

	public String getFormat() {
		return format;
	}

	public String getFormatNumber() {
		try {
			return format.split(":")[1];
		} catch (Exception e) {
			return "";
		}
	}

	public Double getMaxX() {
		return envelope.get("maxx");
	}

	public Double getMaxY() {
		return envelope.get("maxy");
	}

	public Double getMinX() {
		return envelope.get("minx");
	}

	public Double getMinY() {
		return envelope.get("miny");
	}
}
