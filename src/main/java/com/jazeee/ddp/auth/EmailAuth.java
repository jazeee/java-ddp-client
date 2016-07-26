package com.jazeee.ddp.auth;

/**
 * For sending Meteor a email/password for authentication
 */
public final class EmailAuth extends AbstractPasswordAuth {
	public EmailAuth(String email, String password) {
		super(email, password);
	}

	@Override
	protected String getKey() {
		return "email";
	}
}
