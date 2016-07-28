package com.jazeee.ddp.client;

public class UnableToConnectException extends Exception {
	private static final long serialVersionUID = 1L;

	public UnableToConnectException(Exception cause) {
		super("Unable to Connect", cause);
	}
}
