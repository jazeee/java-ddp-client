package com.jazeee.ddp.listeners;

import com.jazeee.common.utils.nullability.NotNull;
import com.jazeee.ddp.messages.client.collections.IDdpCollectionMessage;

public interface IDdpCollectionListener extends IDdpListener {
	public void processMessage(@NotNull IDdpCollectionMessage ddpCollectionMessage);

}
