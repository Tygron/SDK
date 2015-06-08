package com.tygron.pub.exceptions;

public class IncompleteResponseException extends RuntimeException {

	private static final long serialVersionUID = -6784952935252398603L;

	public IncompleteResponseException() {
		super();
	}

	public IncompleteResponseException(String message) {
		super(message);
	}

	public IncompleteResponseException(String message, Throwable cause) {
		super(message, cause);
	}

	public IncompleteResponseException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public IncompleteResponseException(Throwable cause) {
		super(cause);
	}

}
