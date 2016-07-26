package com.jazeee.ddp.listeners;

public interface IDDPHeartbeatListener {

	/**
	 * Callback for receiving a Ping back from the server
	 * 
	 * @param pongId pong ID (mandatory)
	 */
	public abstract void onPing(String pongId);

	/**
	 * Callback for receiving a Pong back from the server
	 * 
	 * @param pingId ping ID (mandatory)
	 */
	public abstract void onPong(String pingId);
}
