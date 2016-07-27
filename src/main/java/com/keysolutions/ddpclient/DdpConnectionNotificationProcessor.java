package com.keysolutions.ddpclient;

import com.jazeee.common.notifier.INotificationProcessor;
import com.jazeee.common.utils.nullability.NotNull;
import com.jazeee.ddp.listeners.IDdpConnectionListener;
import com.jazeee.ddp.messages.client.connection.IDdpClientConnectionMessage;

public class DdpConnectionNotificationProcessor implements INotificationProcessor<IDdpConnectionListener, IDdpClientConnectionMessage> {
	@Override
	public void notifyListener(@NotNull IDdpConnectionListener listener, @NotNull IDdpClientConnectionMessage message) {
		assert (listener != null);
		assert (message != null);
		listener.processMessage(message);
	}
}
