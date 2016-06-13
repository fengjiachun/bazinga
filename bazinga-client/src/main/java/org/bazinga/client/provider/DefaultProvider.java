package org.bazinga.client.provider;

import static org.bazinga.common.protocol.BazingaProtocol.MAGIC;
import static org.bazinga.common.protocol.BazingaProtocol.PUBLISH_SERVICE;
import static org.bazinga.common.serialization.SerializerHolder.serializerImpl;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.MessageToByteEncoder;

import org.bazinga.common.message.Message;
import org.bazinga.common.message.RegistryInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * provider端代码
 */
public class DefaultProvider {
	
	protected static final Logger logger = LoggerFactory.getLogger(DefaultProvider.class); 
	
	private MessageEncoder messageEncoder = new MessageEncoder();
	
	private ProviderHandler provider = new ProviderHandler();
	
	private RegistryInfo info;
	
	public DefaultProvider(RegistryInfo info) {
		this.info = info;
	}

	public void connect(int port, String host) throws Exception {
        
        
	     // Configure the client.
	        EventLoopGroup group = new NioEventLoopGroup();
	        ChannelFuture future = null;
	        try {
	            Bootstrap b = new Bootstrap();
	            b.group(group)
	             .channel(NioSocketChannel.class)
	             .option(ChannelOption.TCP_NODELAY, true)
	             .handler(new ChannelInitializer<SocketChannel>() {
	                 @Override
	                 public void initChannel(SocketChannel ch) throws Exception {
	                     ch.pipeline().addLast("encoder", messageEncoder);
                         ch.pipeline().addLast(provider);
	                 }
	             });

	            future = b.connect(host, port).sync();
	            future.channel().closeFuture().sync();
	        } finally {
	          group.shutdownGracefully();
	        }
	    }

	 @ChannelHandler.Sharable
	 static class MessageEncoder extends MessageToByteEncoder<Message> {

	        @Override
	        protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
	            byte[] bytes = serializerImpl().writeObject(msg);

	            out.writeShort(MAGIC)
	                    .writeByte(msg.sign())
	                    .writeByte(0)
	                    .writeLong(0)
	                    .writeInt(bytes.length)
	                    .writeBytes(bytes);
	        }
	  }
	 
	 class ProviderHandler extends ChannelInboundHandlerAdapter {
		 
		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			 logger.info("channel has active");
			 Channel channel = ctx.channel();
			 
			 Message msg = new Message();
			 msg.sign(PUBLISH_SERVICE);
			 msg.data(info);
			 channel.writeAndFlush(msg).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
			 
		}
		 
		 @Override
		 public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
					throws Exception {
				logger.error("occur exception:{}",cause.getMessage());
				ctx.channel().close();
		}
	 }

	public void providerRegistryInfo(RegistryInfo registryInfo) {
		this.info = registryInfo;
	}
	
}
