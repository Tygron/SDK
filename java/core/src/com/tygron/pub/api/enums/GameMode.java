package com.tygron.pub.api.enums;

public enum GameMode {
		SINGLE_PLAYER(
			AccountType.SINGLEPLAYER,
			ClientType.VIEWER),
		MULTI_PLAYER(
			AccountType.MULTIPLAYER,
			ClientType.ADMIN,
			ClientType.VIEWER,
			ClientType.BEAMER),
		EDITOR(
			AccountType.EDITOR,
			ClientType.EDITOR);

	private final ClientType[] clientTypes;
	private final AccountType minimumAccountType;

	private GameMode(final AccountType minimumAccountType, final ClientType... clientTypes) {
		this.clientTypes = clientTypes;
		this.minimumAccountType = minimumAccountType;
	}

	public ClientType getMainClientType() {
		for (ClientType c : clientTypes) {
			return c;
		}
		return null;
	}

	public AccountType getMinimumAccountType() {
		return this.minimumAccountType;
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
