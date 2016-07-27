package com.jazeee.ddp.messages.client.subscriptions;

import com.jazeee.ddp.messages.DdpErrorField;

public class DdpNoSubscriptionMessage implements IDdpSubscriptionMessage {
	private final String id;
	private final DdpErrorField error;

	public DdpNoSubscriptionMessage() {
		super();
		this.id = "";
		this.error = null;
	}

	public String getId() {
		return id;
	}

	public DdpErrorField getError() {
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
