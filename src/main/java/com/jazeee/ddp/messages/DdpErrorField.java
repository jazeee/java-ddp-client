package com.jazeee.ddp.messages;

import org.apache.commons.lang3.math.NumberUtils;

public class DdpErrorField {
	private final String error;
	private final String reason;
	private final String message;
	private final String details;
	private final String errorType;

	public DdpErrorField(String error) {
		super();
		this.error = error;
		this.reason = "";
		this.message = "";
		this.details = "";
		this.errorType = "";
	}

	public String getError() {
		return error;
	}

	public long getErrorCodeIfPossible() {
		if (NumberUtils.isNumber(error)) {
			return Math.round(Double.parseDouble(error));
		}
		return -1;
	}

	public String getReason() {
		return reason;
	}

	public String getMessage() {
		return message;
	}

	public String getDetails() {
		return details;
	}

	public String getErrorType() {
		return errorType;
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
