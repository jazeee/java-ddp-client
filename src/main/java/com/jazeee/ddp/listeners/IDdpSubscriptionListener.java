package com.jazeee.ddp.listeners;

import com.jazeee.ddp.messages.client.subscriptions.IDdpSubscriptionMessage;

public interface IDdpSubscriptionListener extends IDdpListener {
	public void processMessage(IDdpSubscriptionMessage ddpSubscriptionMessage);
}
