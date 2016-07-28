package com.jazeee.ddp.client.notificationProcessors;

import com.jazeee.common.notifier.INotificationProcessor;
import com.jazeee.common.utils.nullability.NotNull;
import com.jazeee.ddp.listeners.IDdpMethodCallListener;
import com.jazeee.ddp.messages.client.methodCalls.IDdpMethodCallMessage;

public class DdpMethodCallNotificationProcessor implements INotificationProcessor<IDdpMethodCallListener, IDdpMethodCallMessage> {
	@Override
	public void notifyListener(@NotNull IDdpMethodCallListener listener, @NotNull IDdpMethodCallMessage message) {
		assert (listener != null);
		assert (message != null);
		listener.processMessage(message);
	}
}
