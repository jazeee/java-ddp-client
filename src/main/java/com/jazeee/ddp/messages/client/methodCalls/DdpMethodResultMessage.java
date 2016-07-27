package com.jazeee.ddp.messages.client.methodCalls;

import com.jazeee.ddp.messages.DdpErrorField;

public class DdpMethodResultMessage implements IDdpMethodCallMessage {
	private final String id;
	private final DdpErrorField error;
	private final Object result;

	public DdpMethodResultMessage() {
		super();
		this.id = "";
		this.error = null;
		this.result = null;
	}

	public String getId() {
		return id;
	}

	public DdpErrorField getError() {
		return error;
	}

	public Object getResult() {
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName() + "  [id=");
		builder.append(id);
		builder.append(", error=");
		builder.append(error);
		builder.append(", result=");
		builder.append(result);
		builder.append("]");
		return builder.toString();
	}
}
