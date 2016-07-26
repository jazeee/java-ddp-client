package com.jazeee.ddp.messages;

import com.jazeee.ddp.messages.client.collections.DdpAddedBeforeCollectionMessage;
import com.jazeee.ddp.messages.client.collections.DdpAddedToCollectionMessage;
import com.jazeee.ddp.messages.client.collections.DdpChangedCollectionMessage;
import com.jazeee.ddp.messages.client.collections.DdpMovedBeforeCollectionMessage;
import com.jazeee.ddp.messages.client.collections.DdpRemovedFromCollectionMessage;
import com.jazeee.ddp.messages.client.connection.DdpConnectFailedMessage;
import com.jazeee.ddp.messages.client.connection.DdpConnectedMessage;
import com.jazeee.ddp.messages.client.methodCalls.DdpMethodUpdatedMessage;
import com.jazeee.ddp.messages.client.methodCalls.DdpResultMessage;
import com.jazeee.ddp.messages.client.subscriptions.DdpNoSubscriptionMessage;
import com.jazeee.ddp.messages.client.subscriptions.DdpSubscriptionReadyMessage;
import com.jazeee.ddp.messages.heartbeat.DdpPingMessage;
import com.jazeee.ddp.messages.heartbeat.DdpPongMessage;

public enum DdpClientMessageType {
	//@formatter:off
	// Connection status
	CONNECTED(DdpConnectedMessage.class), 
	FAILED(DdpConnectFailedMessage.class),
	// Method calls
	RESULT(DdpResultMessage.class),
	UPDATED(DdpMethodUpdatedMessage.class),
	// Subscriptions
	READY(DdpSubscriptionReadyMessage.class),
	NOSUB(DdpNoSubscriptionMessage.class),
	// Collections
	ADDED(DdpAddedToCollectionMessage.class), 
	REMOVED(DdpRemovedFromCollectionMessage.class), 
	CHANGED(DdpChangedCollectionMessage.class), 
	ADDED_BEFORE(DdpAddedBeforeCollectionMessage.class), 
	MOVED_BEFORE(DdpMovedBeforeCollectionMessage.class), 
	// Heartbeat
	PING(DdpPingMessage.class), 
	PONG(DdpPongMessage.class);
	//@formatter:on
	private final String ddpKey;
	private final Class<? extends IDdpClientMessage> ddpMessageType;

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
