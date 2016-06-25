package org.bazinga.client.processor;

import io.netty.channel.Channel;

import org.bazinga.common.message.Request;
import org.bazinga.common.message.ResultMessageWrapper;

public abstract class AbstractProviderProcessor implements ProviderProcessor {
	
	@Override
	public void handleException(Channel channel, Request request, Throwable cause) {
		ResultMessageWrapper requestMessageWrapper = new ResultMessageWrapper();
	}

}
