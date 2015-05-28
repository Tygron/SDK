package com.tygron.pub.api.enums.events;

import com.tygron.pub.api.enums.EventType.SessionEventType;

public enum AnswerEvent
	implements
	SessionEventType {

	UPGRADE_APPROVAL(
		"AnswerEvent/UPGRADE_APPROVAL/");

	private final String urlSegment;

	private AnswerEvent(String urlSegment) {
		this.urlSegment = urlSegment;
	}

	@Override
	public String url() {
		return urlSegment;
	}
}
