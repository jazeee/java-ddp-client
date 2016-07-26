package com.jazeee.ddp.auth;

import java.util.HashMap;
import java.util.Map;

abstract class AbstractPasswordAuth {
	private final Map<String, String> user = new HashMap<String, String>();
	private final String password;

	protected abstract String getKey();

	public AbstractPasswordAuth(String email, String password) {
		super();
		this.user.put(getKey(), email);
		this.password = password;
	}

	public String getUserName() {
		return user.get(getKey());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Auth user=");
		builder.append(getUserName());
		builder.append(", password=***OBSCURED***");
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AbstractPasswordAuth other = (AbstractPasswordAuth) obj;
		if (password == null) {
			if (other.password != null) {
				return false;
			}
		} else if (!password.equals(other.password)) {
			return false;
		}
		if (user == null) {
			if (other.user != null) {
				return false;
			}
		} else if (!user.equals(other.user)) {
			return false;
		}
		return true;
	}
}
