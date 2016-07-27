package com.jazeee.ddp.messages;

public class DdpTopLevelErrorMessage implements IDdpClientMessage {
	private final String reason;
	private final String offendingMessage;
	private final String javaSource;

	public DdpTopLevelErrorMessage() {
		super();
		this.reason = "";
		this.offendingMessage = "";
		this.javaSource = "";
	}

	public String getReason() {
		return reason;
	}

	public String getOffendingMessage() {
		return offendingMessage;
	}

	public String getJavaSource() {
		return javaSource;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName() + " [reason=");
		builder.append(reason);
		builder.append(", offendingMessage=");
		builder.append(offendingMessage);
		builder.append(", javaSource=");
		builder.append(javaSource);
		builder.append("]");
		return builder.toString();
	}
}
