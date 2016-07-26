package com.jazeee.ddp.messages.heartbeat;

import com.jazeee.ddp.messages.IDdpClientMessage;

public class DdpPingMessage implements IDdpClientMessage {
	private final String id;

	public DdpPingMessage() {
		super();
		this.id = "";
	}

	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName() + " [id=");
		builder.append(id);
		builder.append("]");
		return builder.toString();
	}
}
