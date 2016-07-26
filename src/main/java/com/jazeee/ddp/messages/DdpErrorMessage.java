package com.jazeee.ddp.messages;

public class DdpErrorMessage {
	private final String error;
	private final String reason;
	private final String details;
	private final String offendingMessage;

	public DdpErrorMessage(String error) {
		super();
		this.error = error;
		this.reason = null;
		this.details = null;
		this.offendingMessage = null;
	}

	public String getError() {
		return error;
	}

	public String getReason() {
		return reason;
	}

	public String getDetails() {
		return details;
	}

	public String getOffendingMessage() {
		return offendingMessage;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DdpErrorMessage [error=");
		builder.append(error);
		builder.append(", reason=");
		builder.append(reason);
		builder.append(", details=");
		builder.append(details);
		builder.append("]");
		return builder.toString();
	}
}
