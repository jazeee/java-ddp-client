package com.jazeee.ddp.listeners;

import com.jazeee.ddp.messages.client.methodCalls.DdpMethodResultMessage;

public interface IDdpMethodListener {

	/**
	 * Callback for method call with all result fields
	 * 
	 * @param resultFields returned results from method call
	 */
	public abstract void onResult(DdpMethodResultMessage ddpResultMessage);

	/**
	 * Callback for method's "updated" event
	 * 
	 * @param callId method call ID
	 */
	public abstract void onUpdated(String callId);

}
