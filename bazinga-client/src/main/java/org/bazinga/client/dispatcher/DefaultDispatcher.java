package org.bazinga.client.dispatcher;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import org.bazinga.client.gather.DefaultResultGather;
import org.bazinga.common.logger.InternalLogger;
import org.bazinga.common.logger.InternalLoggerFactory;
import org.bazinga.common.message.Request;

/**
 * 默认的分发请求的中心
 * DefaultDispatcher 职责有两点
 * 1)记录发送请求的channel，request,timeout 这样做的好处就是因为当
 * @author BazingaLyn
 * @copyright fjc
 * @time
 */
public class DefaultDispatcher {
	
	private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultDispatcher.class);
	
	public DefaultResultGather dispatcher(Channel channel,Request request,long timeout){
		
		final Channel _channel = channel;
		DefaultResultGather defaultResultGather = new DefaultResultGather(channel, request,timeout);
		
		_channel.writeAndFlush(request).addListener(new ChannelFutureListener() {  
            public void operationComplete(ChannelFuture future) throws Exception {  
                if(!future.isSuccess()) {  
                    logger.info("send fail,reason is {}",future.cause().getMessage());  
                }  
                  
            }  
        });
		return defaultResultGather;
		
	}

}
