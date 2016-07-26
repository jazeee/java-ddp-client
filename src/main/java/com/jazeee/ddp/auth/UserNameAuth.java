package com.jazeee.ddp.auth;

/**
 * For sending Meteor a username/password for authentication
 */
public final class UserNameAuth extends AbstractPasswordAuth {
	public UserNameAuth(String userName, String password) {
		super(userName, password);
	}

	@Override
	protected String getKey() {
		return "username";
	}
}
