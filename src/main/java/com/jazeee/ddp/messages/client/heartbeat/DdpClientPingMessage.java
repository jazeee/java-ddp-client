package com.jazeee.ddp.messages.client.heartbeat;

import com.jazeee.ddp.messages.server.heartbeat.DdpServerPongMessage;

public class DdpClientPingMessage implements IDdpClientHeartbeatMessage {
	private final String id;

	public DdpClientPingMessage() {
		super();
		this.id = "";
	}

	@Override
	public String getId() {
		return id;
	}

	public DdpServerPongMessage createPongResponse() {
		return new DdpServerPongMessage(id);
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
