package com.jazeee.ddp.listeners;

import java.util.Map;

public interface IDDPMethodListener {

	/**
	 * Callback for method call with all result fields
	 * 
	 * @param resultFields returned results from method call
	 */
	public abstract void onResult(Map<String, Object> resultFields);

	/**
	 * Callback for method's "updated" event
	 * 
	 * @param callId method call ID
	 */
	public abstract void onUpdated(String callId);

}
