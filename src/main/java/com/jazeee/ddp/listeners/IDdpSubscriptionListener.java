package com.jazeee.ddp.listeners;

import com.jazeee.ddp.messages.client.subscriptions.DdpNoSubscriptionMessage;

public interface IDdpSubscriptionListener extends IDdpListener {

	/**
	 * Callback for method's "ready" event (for subscriptions)
	 * 
	 * @param callId method call ID
	 */
	public abstract void onSubscriptionReady(String callId);

	void onNoSub(DdpNoSubscriptionMessage ddpNoSubscriptionMessage);

}
