package com.jazeee.ddp.client;

public interface IDdpClient {

	/**
	 * Called after initial web-socket connection. Sends back a connection confirmation message to the Meteor server.
	 */
	public abstract void onConnectionOpened();

	/**
	 * Called when connection is closed
	 * 
	 * @param code WebSocket Error code
	 * @param reason Reason msg for error
	 * @param isDisconnectedByRemote Whether error is from remote side
	 */
	public abstract void onConnectionClosed(int code, String reason, boolean isDisconnectedByRemote);

	/**
	 * Error handling for any errors over the web-socket connection
	 * 
	 * @param ex exception to convert to event
	 */
	public abstract void onError(Exception ex);

	/**
	 * Notifies observers of this DDP client of messages received from the Meteor server
	 * 
	 * @param jsonMessage received msg from websocket
	 */
	public abstract void onReceived(String jsonMessage);

}