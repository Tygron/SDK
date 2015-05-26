package com.tygron.pub.api.data.item;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tygron.pub.utils.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Building extends Item {
	public static enum State {
			NOTHING,
			WAITING_FOR_DATE,
			REQUEST_ZONING_APPROVAL,
			REQUEST_CONSTRUCTION_APPROVAL,
			CONSTRUCTION_APPROVAL,
			PENDING_CONSTRUCTION,
			CONSTRUCTING,
			READY,
			WAITING_FOR_DEMOLISH_DATE,
			PENDING_DEMOLISHING,
			DEMOLISHING,
			DEMOLISH_FINISHED;
	}

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
