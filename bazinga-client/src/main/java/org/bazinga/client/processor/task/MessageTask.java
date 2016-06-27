package org.bazinga.client.processor.task;

import static org.bazinga.common.serialization.SerializerHolder.serializerImpl;
import static org.bazinga.common.utils.Reflects.fastInvoke;
import static org.bazinga.common.utils.Reflects.findMatchingParameterTypes;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.util.List;

import org.bazinga.client.processor.DefaultProviderProcessor;
import org.bazinga.client.provider.model.ServiceWrapper;
import org.bazinga.common.message.Request;
import org.bazinga.common.message.RequestMessageWrapper;
import org.bazinga.common.message.Response;
import org.bazinga.common.message.ResultMessageWrapper;
import org.bazinga.common.message.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        
        
		byte[] bytes = _request.bytes();
		messageWrapper = serializerImpl().readObject(bytes, RequestMessageWrapper.class);
		_request.setMessageWrapper(messageWrapper);
		
		String serviceName = _request.getMessageWrapper().getServiceName();
		
		logger.info("request service name is {}",serviceName);
		
		Object invokeResult = null;
		
		ServiceWrapper serviceWrapper = _processor.lookupService(serviceName);
		String methodName = serviceWrapper.getMethodName();
		List<Class<?>[]> parameterTypesList = serviceWrapper.getParamters();
		Object provider = serviceWrapper.getServiceProvider();
		
		//请求端传递过来的方法参数
		Object[] args = _request.getMessageWrapper().getArgs();
		
		Class<?>[] parameterTypes = findMatchingParameterTypes(parameterTypesList, args);
		
		invokeResult = fastInvoke(provider, methodName, parameterTypes, args);
		

		ResultMessageWrapper requestMessageWrapper = new ResultMessageWrapper();

		requestMessageWrapper.setResult(invokeResult);
		byte[] results = serializerImpl().writeObject(requestMessageWrapper);
		final Response response = Response.newInstance(_request.invokeId(),
				Status.OK, results);

		channel.writeAndFlush(response).addListener(new ChannelFutureListener() {

			public void operationComplete(ChannelFuture future) throws Exception {
				if(future.isSuccess()){
					logger.info("request {} get success response {}",request,response);
				}else{
					logger.info("request {} get failed response {}",request,response);
				}
			}
		});

	}

}
