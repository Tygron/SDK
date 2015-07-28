package com.tygron.pub.api.enums.events;

import com.tygron.pub.api.enums.EventType.SessionEventType;

public enum SessionEvent
	implements
	SessionEventType {

		SET_MAP_SIZE(
			"EditorEventType/GENERATE_INITIAL_TILES/"),
		GENERATE_GAME(
			"EditorEventType/START_WORLD_CREATION/"),

		STAKEHOLDER_SELECT(
			"PlayerEventType/STAKEHOLDER_SELECT/"),
		STAKEHOLDER_RELEASE(
			"LogicEventType/STAKEHOLDER_RELEASE/"),
		SETTINGS_ALLOW_GAME_INTERACTION(
			"LogicEventType/SETTINGS_ALLOW_GAME_INTERACTION/"),

		STAKEHOLDER_SET_LOCATION(
			"PlayerEventType/STAKEHOLDER_SET_LOCATION"),

		BUILDING_PLAN_CONSTRUCTION(
			"PlayerEventType/BUILDING_PLAN_CONSTRUCTION/"),
		BUILDING_PLAN_UPGRADE(
			"PlayerEventType/BUILDING_PLAN_UPGRADE/"),
		BUILDING_PLAN_DEMOLISH(
			"PlayerEventType/BUILDING_PLAN_DEMOLISH/"),
		BUILDING_PLAN_DEMOLISH_COORDINATES(
			"PlayerEventType/BUILDING_PLAN_DEMOLISH_COORDINATES/"),
		BUILDING_REVERT_COORDINATES(
			"PlayerEventType/BUILDING_REVERT_COORDINATES"),

		MESSAGE_ANSWER(
			"PlayerEventType/MESSAGE_ANSWER"),
		POPUP_ANSWER(
			"PlayerEventType/POPUP_ANSWER"),
		POPUP_ANSWER_WITH_DATE(
			"PlayerEventType/POPUP_ANSWER_WITH_DATE"),

		TERRAIN_SET_POLYGON(
			"LogicEventType/TERRAIN_SET_POLYGON");

	private final String urlSegment;

	private SessionEvent(String urlSegment) {
		this.urlSegment = urlSegment;
	}

	@Override
	public String url() {
		return urlSegment;
	}
}
