package com.jazeee.ddp.messages.client.methodCalls;

import java.util.Collections;
import java.util.List;

public class DdpMethodUpdatedMessage implements IDdpMethodCallMessage {
	private final List<String> methods;

	public DdpMethodUpdatedMessage() {
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
