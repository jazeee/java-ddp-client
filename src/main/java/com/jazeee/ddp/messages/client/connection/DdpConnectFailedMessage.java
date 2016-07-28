package com.jazeee.ddp.messages.client.connection;


public class DdpConnectFailedMessage implements IDdpClientConnectionMessage {
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
