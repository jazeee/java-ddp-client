package com.jazeee.ddp.messages.client.subscriptions;

import com.jazeee.ddp.messages.IDdpClientMessage;

public class DdpNoSubscriptionMessage implements IDdpClientMessage {
	private final String id;
	private final Error error;

	public DdpNoSubscriptionMessage() {
		super();
		this.id = "";
		this.error = null;
	}

	public String getId() {
		return id;
	}

	public Error getError() {
		return error;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName() + " [");
		builder.append("id=");
		builder.append(id);
		builder.append(", error=");
		builder.append(error);
		builder.append("]");
		return builder.toString();
	}
}
