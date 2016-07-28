package com.keysolutions.ddpclient;

import com.jazeee.ddp.messages.client.connection.IDdpClientConnectionMessage;

public class DdpDisconnectedMessage implements IDdpClientConnectionMessage {
	private final String code;
	private final String reason;
	private final Boolean isDisconnectedByRemote;

	public DdpDisconnectedMessage(String code, String reason, boolean isDisconnectedByRemote) {
		super();
		this.code = code;
		this.reason = reason;
		this.isDisconnectedByRemote = isDisconnectedByRemote;
	}

	public String getCode() {
		return code;
	}

	public String getReason() {
		return reason;
	}

	public Boolean getRemote() {
		return isDisconnectedByRemote;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName() + " [code=");
		builder.append(code);
		builder.append(", reason=");
		builder.append(reason);
		builder.append(", isDisconnectedByRemote=");
		builder.append(isDisconnectedByRemote);
		builder.append("]");
		return builder.toString();
	}
}
