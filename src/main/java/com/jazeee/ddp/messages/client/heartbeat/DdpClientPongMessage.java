package com.jazeee.ddp.messages.client.heartbeat;


public class DdpClientPongMessage implements IDdpClientHeartbeatMessage {
	private final String id;

	public DdpClientPongMessage() {
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
