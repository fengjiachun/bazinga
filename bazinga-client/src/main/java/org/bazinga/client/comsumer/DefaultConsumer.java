package org.bazinga.client.comsumer;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import org.bazinga.common.message.SubScribeInfo;


public class DefaultConsumer extends DefaultConsumerRegistry {

	public DefaultConsumer(SubScribeInfo info) {
		super(info);
	}
	
	public Channel connectToProvider(int port, String host) {

		EventLoopGroup group = new NioEventLoopGroup();
		ChannelFuture future = null;
		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class)
					.option(ChannelOption.TCP_NODELAY, true)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch)
								throws Exception {
						}
					});

			future = b.connect(host, port).sync();
			
			return future.channel();
		} catch (InterruptedException e) {
			logger.error("connection occur exception :{}",e.getMessage());
		} finally {
			group.shutdownGracefully();
		}
		return null;
	}
	
	

}
