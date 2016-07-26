package com.jazeee.ddp.messages.client.connection;

import com.jazeee.ddp.messages.IDdpClientMessage;

public class DdpConnectFailedMessage implements IDdpClientMessage {
	private final String version;

	public DdpConnectFailedMessage() {
		super();
		this.version = "";
	}

	public String getSuggestedDdpVersion() {
		return version;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName() + " [getSuggestedDdpVersion=");
		builder.append(version);
		builder.append("]");
		return builder.toString();
	}
}
