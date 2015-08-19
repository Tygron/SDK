package com.tygron.pub.api.enums;

public enum GameMode {
		SINGLE_PLAYER(
			ClientType.VIEWER),
		MULTI_PLAYER(
			ClientType.ADMIN,
			ClientType.VIEWER,
			ClientType.BEAMER),
		EDITOR(
			ClientType.EDITOR);

	private final ClientType[] clientTypes;

	private GameMode(final ClientType... clientTypes) {
		this.clientTypes = clientTypes;
	}

	public ClientType getMainClientType() {
		for (ClientType c : clientTypes) {
			return c;
		}
		return null;
	}

	public boolean isValidClientType(final String clientType) {
		ClientType provided = ClientType.valueOf(clientType);
		for (ClientType c : clientTypes) {
			if (c.equals(provided)) {
				return true;
			}
		}
		return false;
	}
}
