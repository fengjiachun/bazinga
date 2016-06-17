package org.bazinga.client.handler;

import static org.bazinga.common.serialization.SerializerHolder.serializerImpl;

import java.rmi.RemoteException;

import org.bazinga.client.gather.DefaultResultGather;
import org.bazinga.common.message.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import org.bazinga.common.message.ResultMessageWrapper;

@Sharable
public class ConsumerHandler extends ChannelInboundHandlerAdapter {
	
	protected static final Logger logger = LoggerFactory.getLogger(ConsumerHandler.class);
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		Channel channel  = ctx.channel();
		
		if(msg instanceof Response){
			try {
				logger.warn("handler这里执行了~", msg, channel);
				handleResponse((Response)msg,channel);
			} catch (Exception e) {
				logger.error("handler response occur exception",e.getMessage());
				throw new RemoteException("handle response occur exception");
			}
			
		}else{
			logger.warn("accpet object is not response type");
		}
		ReferenceCountUtil.release(msg);
	}

	private void handleResponse(Response response, Channel channel) {
		response.result(serializerImpl().readObject(response.bytes(), ResultMessageWrapper.class));
		response.bytes(null);
		DefaultResultGather.received(channel, response);
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
