package com.jazeee.ddp.messages.client.heartbeat;

import com.jazeee.ddp.messages.IDdpClientMessage;

public interface IDdpClientHeartbeatMessage extends IDdpClientMessage {
	String getId();
}
