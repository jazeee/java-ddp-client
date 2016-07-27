package com.jazeee.ddp.listeners;

import com.jazeee.ddp.messages.DdpClientMessages;

public interface IDdpListener extends IDdpHeartbeatListener, IDdpMethodListener, IDdpCollectionListener, IDdpSubscriptionListener {
	public void onDdpMessage(DdpClientMessages ddpClientMessages);
}