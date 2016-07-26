package com.jazeee.ddp;

import java.util.Map;

public interface IDDPListener extends IDDPHeartbeatListener, IDDPMethodListener, IDDPCollectionListener, IDDPSubscriptionListener {
	public void onDDPMessage(Map<String, Object> jsonFields);
}