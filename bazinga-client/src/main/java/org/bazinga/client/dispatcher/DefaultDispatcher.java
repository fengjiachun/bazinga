package org.bazinga.client.dispatcher;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import org.bazinga.client.gather.DefaultResultGather;
import org.bazinga.common.logger.InternalLogger;
import org.bazinga.common.logger.InternalLoggerFactory;
import org.bazinga.common.message.Request;

public class DefaultDispatcher {
	
	private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultDispatcher.class);
	
	public DefaultResultGather dispatcher(Channel channel,Request request,long timeout){
		//TODO 每个方法特殊的超时时间没有设置
		final Channel _channel = channel;
		DefaultResultGather defaultResultGather = new DefaultResultGather(channel, request,timeout);
		logger.info("ready send request {} to provider",request);
		_channel.writeAndFlush(request).addListener(new ChannelFutureListener() {  
            public void operationComplete(ChannelFuture future) throws Exception {  
                if(!future.isSuccess()) {  
                    logger.info("send fail,reason is {}",future.cause().getMessage());  
                }  
                  
            }  
        });
		
		logger.info("over send");
		return defaultResultGather;
		
	}

}
