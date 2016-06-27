package org.bazinga.client.processor.provider.exception;

public class BadRequestException extends RuntimeException {

	private static final long serialVersionUID = 1511552770469529313L;

	public BadRequestException() {}

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadRequestException(Throwable cause) {
        super(cause);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
