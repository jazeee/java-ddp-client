package com.jazeee.ddp.messages;

public class DdpTopLevelErrorMessage implements IDdpClientMessage {
	private final String reason;
	private final String offendingMessage;
	private final String source;
	private final String errormsg;

	public DdpTopLevelErrorMessage() {
		super();
		this.reason = "";
		this.offendingMessage = "";
		this.source = "";
		this.errormsg = "";
	}

	public String getReason() {
		return reason;
	}

	public String getOffendingMessage() {
		return offendingMessage;
	}

	public String getSource() {
		return source;
	}

	public String getErrorMessage() {
		return errormsg;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName() + " [reason=");
		builder.append(reason);
		builder.append(", offendingMessage=");
		builder.append(offendingMessage);
		builder.append(", source=");
		builder.append(source);
		builder.append(", errormsg=");
		builder.append(errormsg);
		builder.append("]");
		return builder.toString();
	}
}
