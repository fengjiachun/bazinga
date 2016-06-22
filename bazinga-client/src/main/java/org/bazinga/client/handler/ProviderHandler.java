package org.bazinga.client.handler;

import static org.bazinga.common.serialization.SerializerHolder.serializerImpl;
import static org.bazinga.common.utils.Reflects.findMatchingParameterTypes;
import static org.bazinga.common.utils.Reflects.fastInvoke;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.util.List;

import org.bazinga.client.provider.DefaultProvider;
import org.bazinga.client.provider.model.ServiceWrapper;
import org.bazinga.common.message.Request;
import org.bazinga.common.message.RequestMessageWrapper;
import org.bazinga.common.message.Response;
import org.bazinga.common.message.ResultMessageWrapper;
import org.bazinga.common.message.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@ChannelHandler.Sharable
public class ProviderHandler extends ChannelInboundHandlerAdapter {

	protected static final Logger logger = LoggerFactory
			.getLogger(ProviderHandler.class);


	private DefaultProvider defaultProvider;
	
	public ProviderHandler(DefaultProvider defaultProvider) {
		this.defaultProvider = defaultProvider;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {

		Channel channel = ctx.channel();
		logger.info("from {} received msg {}", channel.remoteAddress(), msg);

		if (msg instanceof Request) {
			final Request request = (Request) msg;
			
			RequestMessageWrapper messageWrapper = null;
			
			byte[] bytes = request.bytes();
			
			messageWrapper = serializerImpl().readObject(bytes, RequestMessageWrapper.class);
			
			request.setMessageWrapper(messageWrapper);
			
			String serviceName = request.getMessageWrapper().getServiceName();
			
			logger.info("request service name is {}",serviceName);
			
			Object invokeResult = null;
			
			ServiceWrapper serviceWrapper = defaultProvider.getProviderContainer().lookupService(serviceName);
			String methodName = serviceWrapper.getMethodName();
			List<Class<?>[]> parameterTypesList = serviceWrapper.getParamters();
			Object provider = serviceWrapper.getServiceProvider();
			
			//请求端传递过来的方法参数
			Object[] args = request.getMessageWrapper().getArgs();
			
			Class<?>[] parameterTypes = findMatchingParameterTypes(parameterTypesList, args);
			
			invokeResult = fastInvoke(provider, methodName, parameterTypes, args);
			

			ResultMessageWrapper requestMessageWrapper = new ResultMessageWrapper();

			requestMessageWrapper.setResult(invokeResult);
			byte[] results = serializerImpl().writeObject(requestMessageWrapper);
			final Response response = Response.newInstance(request.invokeId(),
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

		ReferenceCountUtil.release(msg);
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		Channel channel = ctx.channel();
		logger.info("来自{}已经建立好连接，准备请求服务", channel.remoteAddress());
		super.channelActive(ctx);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		logger.error("接收到调用者请求的时候发生了异常{}",cause.getMessage());
		ctx.channel().close();
	}

}
