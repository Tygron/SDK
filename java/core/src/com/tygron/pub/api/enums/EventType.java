package com.tygron.pub.api.enums;

/**
 * This is not an enum of itself, but by having event-related enums implement this interface, it's possible to
 * make the event enum effectively extensible.
 * @author Rudolf
 *
 */
public interface EventType {

	public interface ServerEventType extends EventType {
	}

	public interface SessionEventType extends EventType {
	}

	public String url();
}
