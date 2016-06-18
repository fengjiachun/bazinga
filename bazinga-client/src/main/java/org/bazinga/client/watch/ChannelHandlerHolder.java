package org.bazinga.client.watch;

import io.netty.channel.ChannelHandler;

public interface ChannelHandlerHolder {

	ChannelHandler[] handlers();
	
}
