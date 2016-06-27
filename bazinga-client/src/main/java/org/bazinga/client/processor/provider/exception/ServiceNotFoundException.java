package org.bazinga.client.processor.provider.exception;

public class ServiceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 4097989511491742441L;

	public ServiceNotFoundException() {}

    public ServiceNotFoundException(String message) {
        super(message);
    }

    public ServiceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceNotFoundException(Throwable cause) {
        super(cause);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
