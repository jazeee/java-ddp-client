package com.jazeee.ddp.messages.client.methodCalls;

import com.jazeee.ddp.messages.IDdpClientMessage;

public class DdpResultMessage implements IDdpClientMessage {
	private final String id;
	private final String error;
	private final String result;

	public DdpResultMessage() {
		super();
		this.id = "";
		this.error = null;
		this.result = null;
	}

	public String getId() {
		return id;
	}

	public String getError() {
		return error;
	}

	public String getResult() {
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
