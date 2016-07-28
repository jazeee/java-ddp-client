package com.jazeee.ddp.listeners;

import com.jazeee.common.utils.nullability.NotNull;
import com.jazeee.ddp.messages.client.heartbeat.IDdpClientHeartbeatMessage;

public interface IDdpHeartbeatListener extends IDdpListener {
	public void processMessage(@NotNull IDdpClientHeartbeatMessage ddpClientHeartbeatMessage);
}
