package org.bazinga.common.transport.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMessageSizeEstimator;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.ThreadFactory;

import org.bazinga.common.utils.NamedThreadFactory;

public abstract class NettyConnector {
	
	private Bootstrap bootstrap;

	private EventLoopGroup worker;
	
	private int nWorkers;
	
	protected volatile ByteBufAllocator allocator;
	
	protected final HashedWheelTimer timer = new HashedWheelTimer(new NamedThreadFactory("bazinga.connector.timer"));
	
	protected void init() {
		ThreadFactory workerFactory = new DefaultThreadFactory("baiznga.connector");
		worker = initEventLoopGroup(nWorkers, workerFactory);

		bootstrap = new Bootstrap().group(worker);

		if (worker instanceof EpollEventLoopGroup) {
			((EpollEventLoopGroup) worker).setIoRatio(100);
		} else if (worker instanceof NioEventLoopGroup) {
			((NioEventLoopGroup) worker).setIoRatio(100);
		}

		bootstrap.option(ChannelOption.ALLOCATOR, allocator).option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT)
				.option(ChannelOption.SO_REUSEADDR, true)
				.channel(NioSocketChannel.class);

		bootstrap.option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.ALLOW_HALF_CLOSURE, false);
	}
	
	protected Bootstrap bootstrap() {
        return bootstrap;
    }

	protected abstract EventLoopGroup initEventLoopGroup(int nWorkers, ThreadFactory workerFactory);
	
	

}
