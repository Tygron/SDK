package com.tygron.pub.api.data.item;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tygron.pub.utils.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Building extends Item {
	private int functionTypeID = StringUtils.NOTHING;
	private int floors = StringUtils.NOTHING;
	private int ownerID = StringUtils.NOTHING;
	private String polygons = StringUtils.EMPTY;
	private String state = StringUtils.EMPTY;

	public int getFloors() {
		return floors;
	}

	public int getFunctionTypeID() {
		return functionTypeID;
	}

	public int getOwnerID() {
		return ownerID;
	}

	public String getPolygons() {
		return polygons;
	}

	public String getState() {
		return state;
	}
}
