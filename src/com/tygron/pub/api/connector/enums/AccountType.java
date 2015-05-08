package com.tygron.pub.api.connector.enums;

import java.util.Arrays;

public enum AccountType {
	INVITE_ONLY, SINGLEPLAYER, MULTIPLAYER, EDITOR, DOMAIN_ADMIN, SUPER_MONITOR, SUPER_USER;

	public AccountType[] andUp() {
		return Arrays.copyOfRange(AccountType.values(), this.ordinal(), AccountType.values().length);
	}
}
