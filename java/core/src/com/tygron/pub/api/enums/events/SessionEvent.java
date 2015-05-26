package com.tygron.pub.api.enums.events;

import com.tygron.pub.api.enums.EventType.SessionEventType;

public enum SessionEvent
	implements
	SessionEventType {

		SET_MAP_SIZE(
			"EditorEventType/GENERATE_INITIAL_TILES/"),
		GENERATE_GAME(
			"ServerGeoEventType/START_WORLD_CREATION/"),

		SAVE_PROJECT_INIT(
			// WARNING: until it's fixed properly: don't use this during single or multiplayer
			"SaveSessionEvent/SAVE_PROJECT_INIT/"),

		STAKEHOLDER_SELECT(
			"PlayerEventType/STAKEHOLDER_SELECT/"),
		SETTINGS_ALLOW_GAME_INTERACTION(
			"SettingsLogicEventType/SETTINGS_ALLOW_GAME_INTERACTION/"),

		STAKEHOLDER_SET_LOCATION(
			"PlayerEventType/STAKEHOLDER_SET_LOCATION"),

		BUILDING_PLAN_CONSTRUCTION(
			"PlayerEventType/BUILDING_PLAN_CONSTRUCTION/"),
		BUILDING_PLAN_DEMOLISH(
			"PlayerEventType/BUILDING_PLAN_DEMOLISH/"),
		BUILDING_PLAN_DEMOLISH_COORDINATES(
			"PlayerEventType/BUILDING_PLAN_DEMOLISH_COORDINATES/"),

		MESSAGE_ANSWER(
			"PlayerEventType/MESSAGE_ANSWER"),
		POPUP_ANSWER(
			"PlayerEventType/POPUP_ANSWER"),
		POPUP_ANSWER_WITH_DATE(
			"PlayerEventType/POPUP_ANSWER_WITH_DATE");

	private final String urlSegment;

	private SessionEvent(String urlSegment) {
		this.urlSegment = urlSegment;
	}

	@Override
	public String url() {
		return urlSegment;
	}
}
