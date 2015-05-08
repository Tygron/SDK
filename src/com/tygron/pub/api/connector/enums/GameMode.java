package com.tygron.pub.api.connector.enums;

public enum GameMode {
	SINGLE_PLAYER(ClientType.VIEWER), MULTI_PLAYER(ClientType.ADMIN, ClientType.VIEWER, ClientType.BEAMER), EDITOR(
			ClientType.EDITOR);

	private final ClientType[] clientTypes;

	private GameMode(final ClientType... clientTypes) {
		this.clientTypes = clientTypes;
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
