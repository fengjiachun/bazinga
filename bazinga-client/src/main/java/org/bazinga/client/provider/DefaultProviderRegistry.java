package org.bazinga.client.provider;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.bazinga.common.protocol.BazingaProtocol.ACK;
import static org.bazinga.common.protocol.BazingaProtocol.MAGIC;
import static org.bazinga.common.protocol.BazingaProtocol.PUBLISH_SERVICE;
import static org.bazinga.common.utils.Constants.WRITER_IDLE_TIME_SECONDS;
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
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.ReplayingDecoder;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadFactory;

import org.bazinga.client.Registry;
import org.bazinga.client.trigger.ConnectorIdleStateTrigger;
import org.bazinga.client.watch.ConnectionWatchdog;
import org.bazinga.common.exception.BazingaException;
import org.bazinga.common.exception.ConnectFailedException;
import org.bazinga.common.idle.IdleStateChecker;
import org.bazinga.common.logger.InternalLogger;
import org.bazinga.common.logger.InternalLoggerFactory;
import org.bazinga.common.message.Acknowledge;
import org.bazinga.common.message.Message;
import org.bazinga.common.message.RegistryInfo;
import org.bazinga.common.protocol.BazingaProtocol;
import org.bazinga.common.transport.netty.NettyConnector;
import org.bazinga.common.utils.NativeSupport;
import org.bazinga.common.utils.SystemClock;

/**
 * provider端代码
 */
public class DefaultProviderRegistry extends NettyConnector implements Registry {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultProviderRegistry.class);

	private MessageEncoder messageEncoder = new MessageEncoder();

	private ProviderRegistryHandler provider = new ProviderRegistryHandler();

	private RegistryInfo info;

	private final boolean nativeEt;

	private final ConnectorIdleStateTrigger idleStateTrigger = new ConnectorIdleStateTrigger();
	
	private final ConcurrentMap<Long, MessageNonAck> messagesNonAcks = new ConcurrentHashMap<Long, DefaultProviderRegistry.MessageNonAck>();

	public DefaultProviderRegistry(RegistryInfo info) {
		this.info = info;
		this.nativeEt = true;
		init();
	}
	
	@Override
	protected void init() {
		super.init();
		bootstrap().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) SECONDS.toMillis(3));
		
	}
	
    protected EventLoopGroup initEventLoopGroup(int nWorkers, ThreadFactory workerFactory) {
    	return isNativeEt() ? new EpollEventLoopGroup(nWorkers, workerFactory) : new NioEventLoopGroup(nWorkers, workerFactory);
    };


	private boolean isNativeEt() {
		return nativeEt && NativeSupport.isSupportNativeET();
	}

	public void connectToRegistryServer(int port, String host) throws Exception {

		final Bootstrap boot = bootstrap();

		final SocketAddress socketAddress = InetSocketAddress.createUnresolved(host, port);

		final ConnectionWatchdog watchdog = new ConnectionWatchdog(boot, timer, socketAddress,null) {

			public ChannelHandler[] handlers() {
				return new ChannelHandler[] { 
						this, 
						new IdleStateChecker(timer, 0, WRITER_IDLE_TIME_SECONDS, 0), 
						idleStateTrigger,
						new MessageDecoder(),
						messageEncoder,
						provider 
						};
			}
		};
		watchdog.setReconnect(true);
		ChannelFuture future;
		try {
			synchronized (bootstrap()) {
				boot.handler(new ChannelInitializer<Channel>() {

					@Override
					protected void initChannel(Channel ch) throws Exception {
						ch.pipeline().addLast(watchdog.handlers());
					}
				});

				future = boot.connect(socketAddress);
			}
			future.sync();
		} catch (Throwable t) {
			throw new ConnectFailedException("connects to [" + host + port + "] fails", t);
		}
	}

	static class MessageDecoder extends ReplayingDecoder<MessageDecoder.State> {

        public MessageDecoder() {
            super(State.HEADER_MAGIC);
        }

        // 协议头
        private final BazingaProtocol header = new BazingaProtocol();

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
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
                        
                        case ACK: {
                            byte[] bytes = new byte[header.bodyLength()];
                            in.readBytes(bytes);

                            Acknowledge ack = serializerImpl().readObject(bytes, Acknowledge.class);
                            out.add(ack);
                            break;
                        }
                        default:
                            throw new BazingaException("解码错误");

                    }
                    checkpoint(State.HEADER_MAGIC);
            }
        }
        
        private static void checkMagic(short magic){
            if (MAGIC != magic) {
                throw new BazingaException("magic不匹配");
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

	@ChannelHandler.Sharable
	static class MessageEncoder extends MessageToByteEncoder<Message> {

		@Override
		protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
			byte[] bytes = serializerImpl().writeObject(msg);

			out.writeShort(MAGIC).writeByte(msg.sign()).writeByte(0).writeLong(0).writeInt(bytes.length).writeBytes(bytes);
		}
	}

	@ChannelHandler.Sharable
	class ProviderRegistryHandler extends ChannelInboundHandlerAdapter {

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			logger.info("channel has active");
			Channel channel = ctx.channel();

			Message msg = new Message();
			msg.sign(PUBLISH_SERVICE);
			msg.data(info);
			channel.writeAndFlush(msg).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
			
			MessageNonAck msgNonAck = new MessageNonAck(msg, channel);
			messagesNonAcks.put(msgNonAck.id, msgNonAck);

		}
		
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			logger.info("收到monitor端的信息，准备解析");
			
			if(msg instanceof Acknowledge){
				logger.info("收到monitor端的Ack信息，无需再次发送注册信息");
				messagesNonAcks.remove(((Acknowledge)msg).sequence());
			}
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			logger.error("occur exception:{}", cause.getMessage());
			ctx.channel().close();
		}
		
		
	}

	public void providerRegistryInfo(RegistryInfo registryInfo) {
		this.info = registryInfo;
	}
	
	static class MessageNonAck {
        private final long id;

        private final Message msg;
        private final Channel channel;
        private final long timestamp = SystemClock.millisClock().now();

        public MessageNonAck(Message msg, Channel channel) {
            this.msg = msg;
            this.channel = channel;

            id = msg.sequence();
        }
    }
	
	@SuppressWarnings("unused")
	private class AckTimeoutScanner implements Runnable {

        public void run() {
            for (;;) {
                try {
                    for (MessageNonAck m : messagesNonAcks.values()) {
                        if (SystemClock.millisClock().now() - m.timestamp > SECONDS.toMillis(10)) {

                            // 移除
                            if (messagesNonAcks.remove(m.id) == null) {
                                continue;
                            }

                            if (m.channel.isActive()) {
                            	logger.warn("准备重新发送注册信息");
                                MessageNonAck msgNonAck = new MessageNonAck(m.msg, m.channel);
                                messagesNonAcks.put(msgNonAck.id, msgNonAck);
                                m.channel.writeAndFlush(m.msg)
                                        .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
                            }
                        }
                    }

                    Thread.sleep(300);
                } catch (Throwable t) {
                    logger.error("An exception has been caught while scanning the timeout acknowledges {}.", t);
                }
            }
        }
    }

}
