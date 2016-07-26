package com.jazeee.ddp.messages.client.connection;

import com.jazeee.ddp.messages.IDdpClientMessage;

public class DdpConnectedMessage implements IDdpClientMessage {
	private final String session;

	public DdpConnectedMessage() {
		super();
		this.session = "";
	}

	public String getSession() {
		return session;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName() + " [session=");
		builder.append(session);
		builder.append("]");
		return builder.toString();
	}
}
