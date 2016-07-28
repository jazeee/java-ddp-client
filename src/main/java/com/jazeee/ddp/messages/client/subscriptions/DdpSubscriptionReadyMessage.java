package com.jazeee.ddp.messages.client.subscriptions;

import java.util.Collections;
import java.util.List;

public class DdpSubscriptionReadyMessage implements IDdpSubscriptionMessage {
	private final List<String> subs;

	public DdpSubscriptionReadyMessage() {
		super();
		this.subs = Collections.emptyList();
	}

	public List<String> getSubscriptionIds() {
		return Collections.unmodifiableList(subs);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName() + " [subs=");
		builder.append(subs);
		builder.append("]");
		return builder.toString();
	}
}
