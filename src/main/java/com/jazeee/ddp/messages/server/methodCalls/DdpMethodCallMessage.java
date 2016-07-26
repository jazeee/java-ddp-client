package com.jazeee.ddp.messages.server.methodCalls;

import java.util.UUID;

import com.jazeee.ddp.messages.IDdpClientMessage;

public class DdpMethodCallMessage implements IDdpClientMessage {
	private final String method;
	private final String params;
	private final String id;
	private final String randomSeed;

	public DdpMethodCallMessage(String method) {
		this(method, null);
	}

	public DdpMethodCallMessage(String method, String jsonParams) {
		super();
		this.method = method;
		this.id = UUID.randomUUID().toString();
		this.params = jsonParams;
		this.randomSeed = null;
	}

	public String getMethod() {
		return method;
	}

	public String getId() {
		return id;
	}

	public String getParamsAsJson() {
		return params;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName() + " [");
		builder.append("method=");
		builder.append(method);
		builder.append(", id=");
		builder.append(id);
		builder.append(", getParamsAsJson=");
		builder.append(params);
		builder.append(", randomSeed=");
		builder.append(randomSeed);
		builder.append("]");
		return builder.toString();
	}
}
