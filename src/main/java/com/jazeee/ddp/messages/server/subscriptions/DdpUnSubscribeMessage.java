package com.jazeee.ddp.messages.server.subscriptions;

import com.jazeee.ddp.messages.DdpServerMessageType;
import com.jazeee.ddp.messages.server.AbstractDdpServerMessage;

public class DdpUnSubscribeMessage extends AbstractDdpServerMessage {
	private final String id;

	public DdpUnSubscribeMessage(String id) {
		super();
		this.id = id;
	}

	@Override
	protected DdpServerMessageType getDdpServerMessageType() {
		return DdpServerMessageType.UNSUB;
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
