package com.jazeee.common.notifier;


public abstract class AbstractNotifier<LISTENER, NOTIFICATION> {

	private final Notifier<LISTENER, NOTIFICATION> exceptionNotifier;

	protected AbstractNotifier(INotificationProcessor<LISTENER, NOTIFICATION> notificationProcessor) {
		exceptionNotifier = new Notifier<>(notificationProcessor);
	}

	public void addListener(LISTENER runtimeExceptionListener) {
		exceptionNotifier.addListener(runtimeExceptionListener);
	}

	public void notifyListeners(NOTIFICATION notification) {
		exceptionNotifier.notifyListeners(notification);
	}
}
