package com.jazeee.ddp.client;

public interface IDdpClient {

	/**
	 * Called after initial web-socket connection. Sends back a connection confirmation message to the Meteor server.
	 */
	public void onConnectionOpened();

	/**
	 * Called when connection is closed
	 * 
	 * @param code WebSocket Error code
	 * @param reason Reason msg for error
	 * @param isDisconnectedByRemote Whether error is from remote side
	 */
	public void onConnectionClosed(int code, String reason, boolean isDisconnectedByRemote);

	/**
	 * Error handling for any errors over the web-socket connection
	 * 
	 * @param throwable throwable to convert to event
	 */
	public void onError(Throwable throwable);

	/**
	 * Notifies observers of this DDP client of messages received from the Meteor server
	 * 
	 * @param jsonMessage received msg from websocket
	 */
	public void onReceived(String jsonMessage);

}