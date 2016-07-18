package org.bazinga.client.watch;

import io.netty.channel.ChannelHandler;

/**
 * 
 * @author BazingaLyn
 *
 * @time
 */
public interface ChannelHandlerHolder {

	ChannelHandler[] handlers();
	
}
