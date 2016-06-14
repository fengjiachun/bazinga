package org.bazinga.client.provider;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

import org.bazinga.client.decoder.ProviderDecoder;
import org.bazinga.client.encoder.ResponseEncoder;
import org.bazinga.common.message.RegistryInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultProvider extends DefaultProviderRegistry {
	
	protected static final Logger logger = LoggerFactory.getLogger(DefaultProvider.class);
	
	private int providerPort;
	
	private ResponseEncoder encoder = new ResponseEncoder();
	
	private ProviderHandler handler = new ProviderHandler();
	

	public DefaultProvider(RegistryInfo info) {
		super(info);
		this.providerPort = info.getAddress().getPort();
	}
	
	public void start(){
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap sbs = new ServerBootstrap().group(bossGroup,workerGroup).channel(NioServerSocketChannel.class).localAddress(new InetSocketAddress(providerPort))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        
                        protected void initChannel(SocketChannel ch) throws Exception {
                        	ch.pipeline().addLast(
                                    //TODO
                                    new ProviderDecoder(),
                                    encoder,
                                    handler);
                        };
                        
                    }).option(ChannelOption.SO_BACKLOG, 128)   
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
             // 绑定端口，开始接收进来的连接
             ChannelFuture future = sbs.bind(providerPort).sync();  
             logger.info("/*********provider begin********/ at port:{}",providerPort);
             future.channel().closeFuture().sync();
        } catch (Exception e) {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
	}
	
	
	

}
