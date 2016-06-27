package org.bazinga.client.processor.provider.task;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.bazinga.common.message.Status.BAD_REQUEST;
import static org.bazinga.common.message.Status.SERVER_BUSY;
import static org.bazinga.common.message.Status.SERVICE_NOT_FOUND;
import static org.bazinga.common.serialization.SerializerHolder.serializerImpl;
import static org.bazinga.common.utils.Reflects.fastInvoke;
import static org.bazinga.common.utils.Reflects.findMatchingParameterTypes;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.util.List;

import org.bazinga.client.metrics.Metrics;
import org.bazinga.client.processor.RejectedRunnable;
import org.bazinga.client.processor.provider.DefaultProviderProcessor;
import org.bazinga.client.processor.provider.exception.BadRequestException;
import org.bazinga.client.processor.provider.exception.ServerBusyException;
import org.bazinga.client.processor.provider.exception.ServiceNotFoundException;
import org.bazinga.client.provider.model.ServiceWrapper;
import org.bazinga.common.message.Request;
import org.bazinga.common.message.RequestMessageWrapper;
import org.bazinga.common.message.Response;
import org.bazinga.common.message.ResultMessageWrapper;
import org.bazinga.common.message.Status;
import org.bazinga.common.utils.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;

public class MessageTask implements RejectedRunnable {

	protected static final Logger logger = LoggerFactory.getLogger(MessageTask.class);

	private DefaultProviderProcessor processor;

	private Channel channel;

	private Request request;	

	// 请求被拒绝次数统计
	private static final Meter rejectionMeter = Metrics.meter("rejection");
	
	// 请求数据大小统计(不包括Jupiter协议头的16个字节)
    private static final Histogram requestSizeHistogram     = Metrics.histogram("request.size");
    
    // 请求处理耗时统计(从request被解码开始, 到response数据被刷到OS内核缓冲区为止)
    private static final Timer processingTimer              = Metrics.timer("processing");
    
    // 响应数据大小统计(不包括Jupiter协议头的16个字节)
    private static final Histogram responseSizeHistogram    = Metrics.histogram("response.size");

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

		try {
			
			byte[] bytes = _request.bytes();
			_request.bytes(null);
			requestSizeHistogram.update(bytes.length);
			
			messageWrapper = serializerImpl().readObject(bytes, RequestMessageWrapper.class);
			_request.setMessageWrapper(messageWrapper);
		} catch (Exception e) {
			rejected(BAD_REQUEST);
			return;
		}

		String serviceName = _request.getMessageWrapper().getServiceName();

		logger.info("request service name is {}", serviceName);

		Object invokeResult = null;

		ServiceWrapper serviceWrapper = _processor.lookupService(serviceName);

		if (serviceWrapper == null) {
			rejected(SERVICE_NOT_FOUND);
			return;
		}

		String methodName = serviceWrapper.getMethodName();
		List<Class<?>[]> parameterTypesList = serviceWrapper.getParamters();
		Object provider = serviceWrapper.getServiceProvider();

		// 请求端传递过来的方法参数
		Object[] args = _request.getMessageWrapper().getArgs();

		Class<?>[] parameterTypes = findMatchingParameterTypes(parameterTypesList, args);

		invokeResult = fastInvoke(provider, methodName, parameterTypes, args);

		ResultMessageWrapper requestMessageWrapper = new ResultMessageWrapper();

		requestMessageWrapper.setResult(invokeResult);
		final byte[] results = serializerImpl().writeObject(requestMessageWrapper);
		final Response response = Response.newInstance(_request.invokeId(), Status.OK, results);

		channel.writeAndFlush(response).addListener(new ChannelFutureListener() {

			public void operationComplete(ChannelFuture future) throws Exception {
				
				long elapsed = SystemClock.millisClock().now() - _request.timestamp();
				if (future.isSuccess()) {
					
					responseSizeHistogram.update(results.length);
					
					processingTimer.update(elapsed, MILLISECONDS);
					logger.info("request {} get success response {}", request, response);
				} else {
					logger.info("request {} get failed response {}", request, response);
				}
			}
		});

	}

	private void rejected(Status status) {
		rejected(status, null);
	}

	private void rejected(Status status, Object signal) {
		final Request _request = request;

		rejectionMeter.mark();
		ResultMessageWrapper requestMessageWrapper = new ResultMessageWrapper();

		switch (status) {
		case SERVER_BUSY:
			requestMessageWrapper.setError(new ServerBusyException());
			break;
		case BAD_REQUEST:
			requestMessageWrapper.setError(new BadRequestException());
		case SERVICE_NOT_FOUND:
			requestMessageWrapper.setError(new ServiceNotFoundException(_request.getMessageWrapper().toString()));
			break;
		default:
			logger.warn("Unexpected status.", status.description());
			return;
		}
		logger.warn("Service rejected: {}.", requestMessageWrapper.getError());

		byte[] bytes = serializerImpl().writeObject(requestMessageWrapper);

		final long invokeId = _request.invokeId();

		final Response response = Response.newInstance(invokeId, Status.OK, bytes);

		channel.writeAndFlush(response).addListener(new ChannelFutureListener() {

			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					logger.info("request error {} get success response {}", request, response);
				} else {
					logger.info("request error {} get failed response {}", request, response);
				}
			}
		});
	}

	@Override
	public void rejected() {
		rejected(SERVER_BUSY, null);
	}

}
