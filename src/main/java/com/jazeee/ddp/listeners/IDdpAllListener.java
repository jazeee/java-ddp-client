package com.jazeee.ddp.listeners;

import com.jazeee.ddp.messages.DdpClientMessages;

public interface IDdpAllListener extends IDdpHeartbeatListener, IDdpConnectionListener, IDdpMethodCallListener, IDdpCollectionListener, IDdpSubscriptionListener {
	public void onDdpMessage(DdpClientMessages ddpClientMessages);
}