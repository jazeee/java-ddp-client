package com.jazeee.ddp.listeners;

import com.jazeee.ddp.messages.DdpClientMessages;

public interface IDDPListener extends IDDPHeartbeatListener, IDDPMethodListener, IDDPCollectionListener, IDDPSubscriptionListener {
	public void onDdpMessage(DdpClientMessages ddpClientMessages);
}