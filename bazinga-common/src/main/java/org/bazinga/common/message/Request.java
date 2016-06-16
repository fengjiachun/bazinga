package org.bazinga.common.message;

import java.util.concurrent.atomic.AtomicLong;

public class Request extends BytesHolder {
	
	private static final AtomicLong invokeIdGenerator = new AtomicLong(0);
	
	private final long invokeId;
	
	private RequestMessageWrapper messageWrapper;
	
	private transient long timestamp;
	
	public Request() {
		this(invokeIdGenerator.getAndIncrement());
	}

	public Request(long invokeId) {
		this.invokeId = invokeId;
	}

	public long invokeId() {
		return invokeId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void timestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public RequestMessageWrapper getMessageWrapper() {
		return messageWrapper;
	}

	public void setMessageWrapper(RequestMessageWrapper messageWrapper) {
		this.messageWrapper = messageWrapper;
	}

	@Override
	public String toString() {
		return "Request [invokeId=" + invokeId + ", messageWrapper="
				+ messageWrapper + ", timestamp=" + timestamp + "]";
	}

}
