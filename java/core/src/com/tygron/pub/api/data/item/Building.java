package com.tygron.pub.api.data.item;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tygron.pub.utils.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Building extends Item {
	public static enum BuildingState {
			NOTHING(
				false,
				false),
			WAITING_FOR_DATE(
				false,
				false),
			REQUEST_ZONING_APPROVAL(
				false,
				false),
			REQUEST_CONSTRUCTION_APPROVAL(
				false,
				false),
			CONSTRUCTION_APPROVED(
				false,
				false),
			CONSTRUCTION_DENIED(
				false,
				false),
			PENDING_CONSTRUCTION(
				false,
				true),
			CONSTRUCTING(
				false,
				true),
			READY(
				true,
				true),
			PENDING_UPGRADE(
				true,
				true),
			WAITING_FOR_DEMOLISH_DATE(
				true,
				true),
			PENDING_DEMOLISHING(
				true,
				false),
			DEMOLISHING(
				false,
				false),
			DEMOLISH_FINISHED(
				false,
				false);

		private boolean isExistingTimeline = false;
		private boolean isExistingPlanning = false;

		private BuildingState(boolean isExistingTimeline, boolean isExistingPlanning) {
			this.isExistingPlanning = isExistingPlanning;
			this.isExistingTimeline = isExistingTimeline;
		}

		public boolean isExisting(boolean timeline) {
			return timeline ? isExistingTimeline : isExistingPlanning;
		}
	}

	private int functionID = StringUtils.NOTHING;
	private int floors = StringUtils.NOTHING;
	private int ownerID = StringUtils.NOTHING;
	private int predecessorID = StringUtils.NOTHING;
	private String polygons = StringUtils.EMPTY;
	private String state = StringUtils.EMPTY;

	public int getFloors() {
		return floors;
	}

	public int getFunctionID() {
		return functionID;
	}

	public int getOwnerID() {
		return ownerID;
	}

	public String getPolygons() {
		return polygons;
	}

	public int getPredecessorID() {
		return predecessorID;
	}

	public String getState() {
		return state;
	}

	public boolean isExisting(boolean timelineGame) {
		return BuildingState.valueOf(state).isExisting(timelineGame);
	}
}
