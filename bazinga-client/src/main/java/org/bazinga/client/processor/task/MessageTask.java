package org.bazinga.client.processor.task;

import static org.bazinga.common.serialization.SerializerHolder.serializerImpl;

import org.bazinga.client.processor.DefaultProviderProcessor;
import org.bazinga.common.message.Request;
import org.bazinga.common.message.RequestMessageWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;

public class MessageTask implements Runnable {
	
	protected static final Logger logger = LoggerFactory.getLogger(MessageTask.class);
	
	
	private DefaultProviderProcessor processor;
	
	private Channel channel;
	
	private Request request;

	public MessageTask(DefaultProviderProcessor defaultProviderProcessor, Channel channel, Request request) {
		this.processor = defaultProviderProcessor;
		this.channel = channel;
		this.request = request;
	}

	@Override
	public void run() {
		
		final DefaultProviderProcessor _processor = processor;
        final Request _request = request;
        
        RequestMessageWrapper messageWrapper = null;
        
        
		byte[] bytes = request.bytes();
		messageWrapper = serializerImpl().readObject(bytes, RequestMessageWrapper.class);
		_request.setMessageWrapper(messageWrapper);

	}

}
