package com.jazeee.ddp.messages;

import com.jazeee.common.utils.JazeeeStringUtils;

public enum DdpServerMessageType {
	//@formatter:off
	// Connection
	CONNECT,
	// Method Calls
	METHOD,
	// Subscribe
	SUB, 
	UNSUB, 
	// Heartbeat
	PING, 
	PONG,
	;
	//@formatter:on
	private final String ddpKey;

	DdpServerMessageType() {
		this.ddpKey = JazeeeStringUtils.toCamelCase(this);
	}

	public String getDdpKey() {
		return ddpKey;
	}

	public static DdpServerMessageType fromKey(String ddpKey) {
		try {
			return DdpServerMessageType.valueOf(ddpKey.toUpperCase());
		} catch (Exception e) {
			// Intentionally ignoring invalid types.
			return null;
		}
	}
}
