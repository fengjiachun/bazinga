package org.bazinga.client.processor.provider.exception;

public class ServerBusyException extends RuntimeException {


	private static final long serialVersionUID = 9008700428389020425L;

	public ServerBusyException() {}

    public ServerBusyException(String message) {
        super(message);
    }

    public ServerBusyException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServerBusyException(Throwable cause) {
        super(cause);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
