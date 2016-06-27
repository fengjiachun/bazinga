package org.bazinga.client.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import org.bazinga.client.processor.DefaultProviderProcessor;
import org.bazinga.common.message.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * 提供者获取到消费者端的请求是的netty的hanndler
 * 注意这边的需要新开一个线程来处理这里的请求
 * @author BazingaLyn
 *
 * @time 2016年6月27日
 */
@ChannelHandler.Sharable
public class ProviderHandler extends ChannelInboundHandlerAdapter {

	protected static final Logger logger = LoggerFactory.getLogger(ProviderHandler.class);


	private DefaultProviderProcessor provider;
	
	public ProviderHandler(DefaultProviderProcessor defaultProviderProcessor) {
		this.provider = defaultProviderProcessor;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {

		Channel channel = ctx.channel();
		logger.info("from {} received msg {}", channel.remoteAddress(), msg);

		if (msg instanceof Request) {
			
			final Request request = (Request) msg;
			try {
				provider.handleRequest(channel, request);
            } catch (Throwable t) {
            	provider.handleException(channel, request, t);
            }
			
			
			
		}else{
			
			logger.warn("accept error msg :{}",msg.getClass());
			
			ReferenceCountUtil.release(msg);
		}

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
