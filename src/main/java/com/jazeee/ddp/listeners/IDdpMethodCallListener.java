package com.jazeee.ddp.listeners;

import com.jazeee.ddp.messages.client.methodCalls.IDdpMethodCallMessage;

public interface IDdpMethodCallListener extends IDdpListener {
	public void processMessage(IDdpMethodCallMessage ddpMethodCallMessage);
}
