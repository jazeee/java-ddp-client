package com.jazeee.ddp.messages.server.subscriptions;

import com.jazeee.ddp.messages.IDdpClientMessage;

public class DdpUnSubscribeMessage implements IDdpClientMessage {
	private final String id;

	public DdpUnSubscribeMessage() {
		super();
		this.id = "";
	}

	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName() + " [");
		builder.append("id=");
		builder.append(id);
		builder.append("]");
		return builder.toString();
	}
}
