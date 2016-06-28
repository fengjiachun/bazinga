package org.bazinga.client.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.rmi.RemoteException;

import org.bazinga.client.processor.consumer.DefaultConsumerProcessor;
import org.bazinga.common.logger.InternalLogger;
import org.bazinga.common.logger.InternalLoggerFactory;
import org.bazinga.common.message.Response;

@Sharable
public class ConsumerHandler extends ChannelInboundHandlerAdapter {
	
	private static final InternalLogger logger = InternalLoggerFactory.getInstance(ConsumerHandler.class);
	
	private final DefaultConsumerProcessor processor;
	
	public ConsumerHandler(DefaultConsumerProcessor defaultConsumerProcessor) {
		this.processor = defaultConsumerProcessor;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		Channel channel  = ctx.channel();
		
		if(msg instanceof Response){
			try {
				
				logger.warn("receive response from channel {} and msg is {}", channel,msg);
				processor.handleResponse(channel,(Response)msg);
			} catch (Exception e) {
				logger.error("handler response occur exception,{}",e.getMessage());
				throw new RemoteException("handle response occur exception");
			}
			
		}else{
			logger.warn("accpet object is not response type");
			ReferenceCountUtil.release(msg);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		logger.info("发送请求失败，发生了异常：{}",cause.getMessage());
		ctx.channel().close();
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		logger.info("调用者的channel激活");
	}

}
