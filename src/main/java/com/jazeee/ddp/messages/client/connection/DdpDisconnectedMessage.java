package com.jazeee.ddp.messages.client.connection;

import com.jazeee.ddp.messages.IDdpClientMessage;

public class DdpDisconnectedMessage implements IDdpClientMessage {
	private final String code;
	private final String reason;
	private final Boolean remote;

	public DdpDisconnectedMessage() {
		super();
		this.code = "";
		this.reason = "";
		this.remote = false;
	}

	public String getCode() {
		return code;
	}

	public String getReason() {
		return reason;
	}

	public Boolean getRemote() {
		return remote;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName() + " [code=");
		builder.append(code);
		builder.append(", reason=");
		builder.append(reason);
		builder.append(", remote=");
		builder.append(remote);
		builder.append("]");
		return builder.toString();
	}
}
