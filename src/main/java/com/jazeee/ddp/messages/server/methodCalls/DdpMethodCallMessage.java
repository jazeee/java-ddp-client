package com.jazeee.ddp.messages.server.methodCalls;

import com.jazeee.ddp.messages.DdpServerMessageType;
import com.jazeee.ddp.messages.server.AbstractDdpServerMessage;

public class DdpMethodCallMessage extends AbstractDdpServerMessage {
	private final String id;
	private final String method;
	private final Object params;
	private final String randomSeed;

	public DdpMethodCallMessage(String id, String method, Object params) {
		super();
		this.id = id;
		this.method = method;
		this.params = params;
		this.randomSeed = null;
	}

	@Override
	protected DdpServerMessageType getDdpServerMessageType() {
		return DdpServerMessageType.METHOD;
	}

	public String getId() {
		return id;
	}

	public String getMethod() {
		return method;
	}

	public Object getParams() {
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
		builder.append(", params=");
		builder.append(params);
		builder.append(", randomSeed=");
		builder.append(randomSeed);
		builder.append("]");
		return builder.toString();
	}
}
