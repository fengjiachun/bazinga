package org.bazinga.client.processor.provider;

import static org.bazinga.common.message.Status.SERVER_ERROR;
import static org.bazinga.common.serialization.SerializerHolder.serializerImpl;
import io.netty.channel.Channel;

import org.bazinga.common.logger.InternalLogger;
import org.bazinga.common.logger.InternalLoggerFactory;
import org.bazinga.common.message.Request;
import org.bazinga.common.message.Response;
import org.bazinga.common.message.ResultMessageWrapper;

/**
 * 服务端当服务于处理请求时的时候，对异常的处理
 * @author BazingaLyncc
 *
 * @time
 */
public abstract class AbstractProviderProcessor implements ProviderProcessor {
	
	private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractProviderProcessor.class);
	
	@Override
	public void handleException(Channel channel, Request request, Throwable cause) {
		
		logger.error("channel {} handler request {} happen an excpetion {}",channel,request,cause);
		ResultMessageWrapper requestMessageWrapper = new ResultMessageWrapper();
		
		requestMessageWrapper.setError(cause);
		byte[] bytes = serializerImpl().writeObject(requestMessageWrapper);
		
		channel.write(Response.newInstance(request.invokeId(), SERVER_ERROR, bytes));
		
	}

}
