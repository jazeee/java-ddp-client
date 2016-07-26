package com.jazeee.ddp.messages.client.methodCalls;

import java.util.Collections;
import java.util.List;

import com.jazeee.ddp.messages.IDdpClientMessage;

public class DdpUpdatedMessage implements IDdpClientMessage {
	private final List<String> methods;

	public DdpUpdatedMessage() {
		super();
		this.methods = Collections.emptyList();
	}

	public List<String> getMethods() {
		return Collections.unmodifiableList(methods);
	}

	@Override
	public String toString() {
		final int maxLen = 10;
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName() + " [methods=");
		builder.append(methods != null ? methods.subList(0, Math.min(methods.size(), maxLen)) : null);
		builder.append("]");
		return builder.toString();
	}
}
