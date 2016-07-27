package com.jazeee.ddp.messages.server;

import com.google.gson.Gson;
import com.jazeee.ddp.messages.DdpServerMessageType;

public abstract class AbstractDdpServerMessage {
	private final String msg;

	protected abstract DdpServerMessageType getDdpServerMessageType();

	public AbstractDdpServerMessage() {
		super();
		this.msg = getDdpServerMessageType().getDdpKey();
	}

	public String toJson() {
		return new Gson().toJson(this);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AbstractDdpServerMessage [msg=");
		builder.append(msg);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((msg == null) ? 0 : msg.hashCode());
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
		AbstractDdpServerMessage other = (AbstractDdpServerMessage) obj;
		if (msg == null) {
			if (other.msg != null) {
				return false;
			}
		} else if (!msg.equals(other.msg)) {
			return false;
		}
		return true;
	}
}
