package com.jazeee.ddp.messages;

import com.jazeee.ddp.messages.client.methodCalls.DdpResultMessage;
import com.jazeee.ddp.messages.heartbeat.DdpPingMessage;
import com.jazeee.ddp.messages.heartbeat.DdpPongMessage;

public enum DdpClientMessageType {
	//@formatter:off
	CONNECTED, 
	FAILED, 
	READY,
	RESULT(DdpResultMessage.class),
	UPDATED,
	SUB, 
	NOSUB, 
	UNSUB, 
	ERROR, 
	CLOSED, 
	ADDED, 
	REMOVED, 
	CHANGED, 
	PING(DdpPingMessage.class), 
	PONG(DdpPongMessage.class);
	//@formatter:on
	private final String ddpKey;
	private final Class<? extends IDdpClientMessage> ddpMessageType;

	DdpClientMessageType() {
		this(null);
	}

	DdpClientMessageType(Class<? extends IDdpClientMessage> ddpMessageType) {
		this.ddpKey = this.name().toLowerCase();
		this.ddpMessageType = ddpMessageType;
	}

	public String getDdpKey() {
		return ddpKey;
	}

	public static DdpClientMessageType fromKey(String ddpKey) {
		try {
			return DdpClientMessageType.valueOf(ddpKey.toUpperCase());
		} catch (Exception e) {
			// Intentionally ignoring invalid types.
			return null;
		}
	}

	public Class<? extends IDdpClientMessage> getDdpMessageClass() {
		return ddpMessageType;
	}
}
