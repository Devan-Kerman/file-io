package net.devtech.filepipeline.impl.util;

import java.io.IOException;

public class ReadOnlySourceException extends IOException {
	public ReadOnlySourceException() {
	}

	public ReadOnlySourceException(String message) {
		super(message);
	}

	public ReadOnlySourceException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReadOnlySourceException(Throwable cause) {
		super(cause);
	}
}
