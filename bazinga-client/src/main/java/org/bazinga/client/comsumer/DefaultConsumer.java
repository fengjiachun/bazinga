package org.bazinga.client.comsumer;

import static org.bazinga.common.protocol.BazingaProtocol.MAGIC;
import static org.bazinga.common.protocol.BazingaProtocol.SUBSCRIBE_SERVICE;
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

import java.util.ArrayList;
import java.util.List;

import org.bazinga.common.message.Message;
import org.bazinga.common.message.SubScribeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultConsumer {
	
	protected static final Logger logger = LoggerFactory.getLogger(DefaultConsumer.class);
	
	private SubScribeInfo info;
	
	private MessageEncoder messageEncoder = new MessageEncoder();
	
	private ConsumerHandler consumerHandler = new ConsumerHandler();
	
	public DefaultConsumer(SubScribeInfo info) {
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
                        ch.pipeline().addLast(consumerHandler);
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
	
	
	class ConsumerHandler extends ChannelInboundHandlerAdapter {
		
		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			
			logger.debug("***********consumer has start**************");
			logger.info("consumer msg :{}",info);
			
			Channel channel = ctx.channel();
			
			Message message = new Message();
			
			message.sign(SUBSCRIBE_SERVICE);
			message.data(info);
			
			channel.writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
			
		}
		
		
		
	}
	
	/**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                // 采用默认值
            }
        }
        SubScribeInfo info = new SubScribeInfo();
        List<String> servicesNames = new ArrayList<String>();
        servicesNames.add("HelloWorldService");
        info.setServiceNames(servicesNames);
        new DefaultConsumer(info).connect(port, "127.0.0.1");
    }

}
