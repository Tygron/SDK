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

		BUILDING_PLAN_CONSTRUCTION(
			"PlayerEventType/BUILDING_PLAN_CONSTRUCTION/");

	private final String urlSegment;

	private SessionEvent(String urlSegment) {
		this.urlSegment = urlSegment;
	}

	@Override
	public String url() {
		return urlSegment;
	}
}
