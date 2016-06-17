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

import org.bazinga.client.decoder.ConsumerDecoder;
import org.bazinga.client.encoder.RequestEncoder;
import org.bazinga.client.handler.ConsumerHandler;
import org.bazinga.common.message.SubScribeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DefaultConsumer extends DefaultConsumerRegistry {
	
	protected static final Logger logger = LoggerFactory.getLogger(DefaultConsumer.class);
	
	private RequestEncoder encoder = new RequestEncoder();
	private ConsumerHandler handler = new ConsumerHandler();

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
							ch.pipeline().addLast(
									new ConsumerDecoder(),
									encoder,
			                        handler);
						}
					});

			future = b.connect(host, port).sync();
			return future.channel();
		} catch (InterruptedException e) {
			logger.error("connection occur exception :{}",e.getMessage());
		}  
		return null;
	}
	
	

}
