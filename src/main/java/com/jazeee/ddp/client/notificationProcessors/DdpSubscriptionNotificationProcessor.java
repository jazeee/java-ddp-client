package com.jazeee.ddp.client.notificationProcessors;

import com.jazeee.common.notifier.INotificationProcessor;
import com.jazeee.common.utils.nullability.NotNull;
import com.jazeee.ddp.listeners.IDdpSubscriptionListener;
import com.jazeee.ddp.messages.client.subscriptions.IDdpSubscriptionMessage;

public class DdpSubscriptionNotificationProcessor implements INotificationProcessor<IDdpSubscriptionListener, IDdpSubscriptionMessage> {
	@Override
	public void notifyListener(@NotNull IDdpSubscriptionListener listener, @NotNull IDdpSubscriptionMessage message) {
		assert (listener != null);
		assert (message != null);
		listener.processMessage(message);
	}
}
