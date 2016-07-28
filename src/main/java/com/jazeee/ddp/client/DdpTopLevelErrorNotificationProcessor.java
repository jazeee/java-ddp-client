package com.jazeee.ddp.client;

import com.jazeee.common.notifier.INotificationProcessor;
import com.jazeee.common.utils.nullability.NotNull;
import com.jazeee.ddp.listeners.IDdpTopLevelErrorListener;
import com.jazeee.ddp.messages.DdpTopLevelErrorMessage;

public class DdpTopLevelErrorNotificationProcessor implements INotificationProcessor<IDdpTopLevelErrorListener, DdpTopLevelErrorMessage> {
	@Override
	public void notifyListener(@NotNull IDdpTopLevelErrorListener listener, @NotNull DdpTopLevelErrorMessage message) {
		assert (listener != null);
		assert (message != null);
		listener.processMessage(message);
	}
}
