package org.bazinga.monitor;

import static org.bazinga.common.protocol.BazingaProtocol.ACK;
import static org.bazinga.common.protocol.BazingaProtocol.MAGIC;
import static org.bazinga.common.protocol.BazingaProtocol.OFFLINE_NOTICE;
import static org.bazinga.common.protocol.BazingaProtocol.PUBLISH_SERVICE;
import static org.bazinga.common.serialization.SerializerHolder.serializerImpl;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;
import java.util.List;

import org.bazinga.common.exception.BazingaException;
import org.bazinga.common.message.Acknowledge;
import org.bazinga.common.message.Message;
import org.bazinga.common.protocol.BazingaProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BazingaMonitor {
	
	protected static final Logger logger = LoggerFactory.getLogger(BazingaMonitor.class); 
	
	private MessageHandler messageHandler = new MessageHandler();
	
	private int port;
	
	public BazingaMonitor(int port) {
        this.port = port;
    }
	
	public void start(){
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap sbs = new ServerBootstrap().group(bossGroup,workerGroup).channel(NioServerSocketChannel.class).localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast("protocoldecoder", new StringDecoder());
                            ch.pipeline().addLast("encoder", new StringEncoder());
                            ch.pipeline().addLast(messageHandler);
                        };
                        
                    }).option(ChannelOption.SO_BACKLOG, 128)   
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
             // 绑定端口，开始接收进来的连接
             ChannelFuture future = sbs.bind(port).sync();  
             logger.info("monitor begin");
             future.channel().closeFuture().sync();
        } catch (Exception e) {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
	
	static class MessageHandler extends ChannelInboundHandlerAdapter {
		
		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			super.channelActive(ctx);
		}
		
	}
	
	
	static class MessageDecoder extends ReplayingDecoder<MessageDecoder.State> {
		
		private final BazingaProtocol header = new BazingaProtocol();
		@Override
		protected void decode(ChannelHandlerContext ctx, ByteBuf in,
				List<Object> out) throws Exception {
			switch (state()) {

            case HEADER_MAGIC:
                checkMagic(in.readShort());             // MAGIC
                checkpoint(State.HEADER_SIGN);
            case HEADER_SIGN:
                header.sign(in.readByte());             // 消息标志位
                checkpoint(State.HEADER_STATUS);
            case HEADER_STATUS:
                in.readByte();                          // no-op
                checkpoint(State.HEADER_ID);
            case HEADER_ID:
                header.id(in.readLong());               // 消息id
                checkpoint(State.HEADER_BODY_LENGTH);
            case HEADER_BODY_LENGTH:
                header.bodyLength(in.readInt());        // 消息体长度
                checkpoint(State.BODY);
            case BODY:
                switch (header.sign()) {
                    case PUBLISH_SERVICE:
                    case OFFLINE_NOTICE: {
                        byte[] bytes = new byte[header.bodyLength()];
                        in.readBytes(bytes);

                        Message msg = serializerImpl().readObject(bytes, Message.class);
                        msg.sign(header.sign());
                        out.add(msg);

                        break;
                    }
                    case ACK: {
                        byte[] bytes = new byte[header.bodyLength()];
                        in.readBytes(bytes);

                        Acknowledge ack = serializerImpl().readObject(bytes, Acknowledge.class);
                        out.add(ack);
                        break;
                    }
                    default:
                    	break;

                }
                checkpoint(State.HEADER_MAGIC);
        
			}
		}
		
		private static void checkMagic(short magic) {
			if (MAGIC != magic) {
                throw new BazingaException();
            }
		}

		enum State {
            HEADER_MAGIC,
            HEADER_SIGN,
            HEADER_STATUS,
            HEADER_ID,
            HEADER_BODY_LENGTH,
            BODY
        }
	}
	
	 public static void main(String[] args) throws Exception {
	        int port;
	        if (args.length > 0) {
	            port = Integer.parseInt(args[0]);
	        } else {
	            port = 8080;
	        }
	        new BazingaMonitor(port).start();
	    }
	
	

}
