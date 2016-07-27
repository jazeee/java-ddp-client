package com.jazeee.utils;

public final class StatelessUtilityClassException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public static final String STATELESS_UTILITY_CLASS_EXCEPTION_STRING = "This is a stateless utility class and can never be instantiated";

	public StatelessUtilityClassException() {
		super(STATELESS_UTILITY_CLASS_EXCEPTION_STRING);
	}
}
