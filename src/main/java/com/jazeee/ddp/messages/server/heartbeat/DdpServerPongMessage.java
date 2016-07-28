package com.jazeee.ddp.messages.server.heartbeat;

import com.jazeee.ddp.messages.DdpServerMessageType;
import com.jazeee.ddp.messages.server.AbstractDdpServerMessage;

public class DdpServerPongMessage extends AbstractDdpServerMessage {
	private final String id;

	public DdpServerPongMessage(String id) {
		super();
		this.id = id;
	}

	@Override
	protected DdpServerMessageType getDdpServerMessageType() {
		return DdpServerMessageType.PONG;
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
