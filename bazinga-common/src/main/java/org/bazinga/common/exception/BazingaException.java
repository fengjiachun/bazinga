package org.bazinga.common.exception;

public class BazingaException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1396804459574239930L;
	
    public BazingaException() {
        super();
    }

    public BazingaException(String message) {
        super(message);
    }

    public BazingaException(String message, Throwable cause) {
        super(message, cause);
    }

    public BazingaException(Throwable cause) {
        super(cause);
    }

}
