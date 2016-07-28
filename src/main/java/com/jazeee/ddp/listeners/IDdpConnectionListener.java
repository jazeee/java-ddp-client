package com.jazeee.ddp.listeners;

import com.jazeee.common.utils.nullability.NotNull;
import com.jazeee.ddp.messages.client.connection.IDdpClientConnectionMessage;

public interface IDdpConnectionListener extends IDdpListener {
	public void processMessage(@NotNull IDdpClientConnectionMessage ddpClientConnectionMessage);
}
