package com.jazeee.ddp.client.notificationProcessors;

import com.jazeee.common.notifier.INotificationProcessor;
import com.jazeee.common.utils.nullability.NotNull;
import com.jazeee.ddp.listeners.IDdpCollectionListener;
import com.jazeee.ddp.messages.client.collections.IDdpCollectionMessage;

public class DdpCollectionNotificationProcessor implements INotificationProcessor<IDdpCollectionListener, IDdpCollectionMessage> {
	@Override
	public void notifyListener(@NotNull IDdpCollectionListener listener, @NotNull IDdpCollectionMessage message) {
		assert (listener != null);
		assert (message != null);
		listener.processMessage(message);
	}
}
