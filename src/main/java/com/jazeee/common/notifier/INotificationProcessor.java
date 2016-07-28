package com.jazeee.common.notifier;

import com.jazeee.common.utils.nullability.NotNull;
import com.jazeee.common.utils.nullability.Nullable;

public interface INotificationProcessor<LISTENER, NOTIFICATION> {
	public void notifyListener(@NotNull LISTENER listener, @Nullable NOTIFICATION listenerNotification);
}
