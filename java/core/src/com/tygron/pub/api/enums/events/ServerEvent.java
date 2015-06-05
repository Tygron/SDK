package com.tygron.pub.api.enums.events;

import com.tygron.pub.api.enums.EventType.ServerEventType;

public enum ServerEvent
	implements
	ServerEventType {

		CREATE_PROJECT(
			"IOServicesEventType/CREATE_NEW_PROJECT/"),
		SET_PERMISSION(
			"IOServicesEventType/SET_PROJECT_PERMISSION/"),
		DELETE_PROJECT(
			"IOServicesEventType/DELETE_PROJECT/"),

		NEW_SESSION(
			"IOServicesEventType/START_NEW_SESSION/"),
		JOIN_SESSION(
			"IOServicesEventType/JOIN_SESSION/"),
		CLOSE_SESSION(
			"IOServicesEventType/CLOSE_SESSION/"),
		KILL_SESSION(
			"IOServicesEventType/KILL_SESSION/"),

		GET_MENU_TREE(
			"IOServicesEventType/GET_MENU_TREE/"),

		GET_JOINABLE_SESSIONS(
			"IOServicesEventType/GET_JOINABLE_SESSIONS/");

	private final String urlSegment;

	private ServerEvent(String urlSegment) {
		this.urlSegment = urlSegment;
	}

	@Override
	public String url() {
		return urlSegment;
	}
}
