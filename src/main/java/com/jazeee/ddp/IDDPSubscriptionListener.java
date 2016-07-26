package com.jazeee.ddp;

import java.util.Map;

public interface IDDPSubscriptionListener {

	/**
	 * Callback for method's "ready" event (for subscriptions)
	 * 
	 * @param callId method call ID
	 */
	public abstract void onSubscriptionReady(String callId);

	/**
	 * Callback for invalid subscription name errors
	 * 
	 * @param callId method call ID
	 * @param errorFields fields holding error info
	 */
	public abstract void onNoSub(String callId, Map<String, Object> errorFields);

}
