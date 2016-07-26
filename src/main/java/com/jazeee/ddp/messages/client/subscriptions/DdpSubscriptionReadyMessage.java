package com.jazeee.ddp.messages.client.subscriptions;

import java.util.Collections;
import java.util.List;

import com.jazeee.ddp.messages.IDdpClientMessage;

public class DdpSubscriptionReadyMessage implements IDdpClientMessage {
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
		builder.append(getClass().getSimpleName() + " [getSubscriptionIds=");
		builder.append(getSubscriptionIds());
		builder.append("]");
		return builder.toString();
	}
}
