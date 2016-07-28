package com.jazeee.ddp.listeners;

import com.jazeee.ddp.messages.DdpTopLevelErrorMessage;

public interface IDdpTopLevelErrorListener extends IDdpListener {
	public void processMessage(DdpTopLevelErrorMessage ddpTopLevelErrorMessage);
}
