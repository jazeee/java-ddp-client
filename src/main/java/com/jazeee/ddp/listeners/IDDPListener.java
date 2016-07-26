package com.jazeee.ddp.listeners;

import java.util.Map;

public interface IDDPListener extends IDDPHeartbeatListener, IDDPMethodListener, IDDPCollectionListener, IDDPSubscriptionListener {
	public void onDDPMessage(Map<String, Object> jsonFields);
}