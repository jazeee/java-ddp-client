package com.jazeee.common.notifier;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.jazeee.common.utils.WeakHashSet;
import com.jazeee.common.utils.nullability.NotNull;
import com.jazeee.common.utils.nullability.Nullable;

public class Notifier<LISTENER, NOTIFICATION> {
	private final Set<LISTENER> weakListeners = Collections.synchronizedSet(new WeakHashSet<LISTENER>());
	private final INotificationProcessor<LISTENER, NOTIFICATION> notificationProcessor;

	public Notifier(INotificationProcessor<LISTENER, NOTIFICATION> notificationProcessor) {
		this.notificationProcessor = notificationProcessor;
	}

	public void addListener(@NotNull LISTENER listener) {
		assert (listener != null);
		weakListeners.add(listener);
	}

	public void removeListener(@NotNull LISTENER listener) {
		assert (listener != null);
		weakListeners.remove(listener);
	}

	public void clearListeners() {
		weakListeners.clear();
	}

	public int getListenerCount() {
		return weakListeners.size();
	}

	public void notifyListeners() {
		notifyListeners(null);
	}

	public void notifyListeners(@Nullable NOTIFICATION listenerNotification) {
		Set<LISTENER> listeners;
		synchronized (weakListeners) {
			listeners = new HashSet<>(weakListeners);
		}
		for (LISTENER listener : listeners) {
			if (null != listener) {
				this.notificationProcessor.notifyListener(listener, listenerNotification);
			}
		}
	}
}
