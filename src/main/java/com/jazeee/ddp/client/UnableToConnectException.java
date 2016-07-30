package com.jazeee.ddp.client;

import java.net.URI;

public class UnableToConnectException extends Exception {
	private static final long serialVersionUID = 1L;

	public UnableToConnectException(Exception cause, URI uri) {
		super("Unable to Connect to " + uri.toString(), cause);
	}
}
