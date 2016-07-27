package com.jazeee.ddp.messages.client.collections;

import com.jazeee.ddp.messages.IDdpClientMessage;

public interface IDdpCollectionMessage extends IDdpClientMessage {
	public String getId();

	public String getCollection();
}
