package org.bazinga.client.comsumer;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.bazinga.common.protocol.BazingaProtocol.ACK;
import static org.bazinga.common.protocol.BazingaProtocol.MAGIC;
import static org.bazinga.common.protocol.BazingaProtocol.OFFLINE_NOTICE;
import static org.bazinga.common.protocol.BazingaProtocol.PUBLISH_SERVICE;
import static org.bazinga.common.protocol.BazingaProtocol.SUBSCRIBE_SERVICE;
import static org.bazinga.common.serialization.SerializerHolder.serializerImpl;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMessageSizeEstimator;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.ThreadFactory;

import org.bazinga.client.Registry;
import org.bazinga.client.preheater.ConectionPreHeater;
import org.bazinga.client.trigger.ConnectorIdleStateTrigger;
import org.bazinga.client.watch.ConnectionWatchdog;
import org.bazinga.common.ack.AcknowledgeEncoder;
import org.bazinga.common.exception.BazingaException;
import org.bazinga.common.exception.ConnectFailedException;
import org.bazinga.common.idle.IdleStateChecker;
import org.bazinga.common.logger.InternalLogger;
import org.bazinga.common.logger.InternalLoggerFactory;
import org.bazinga.common.message.Acknowledge;
import org.bazinga.common.message.Message;
import org.bazinga.common.message.ProviderInfo;
import org.bazinga.common.message.ProviderInfos;
import org.bazinga.common.message.SubScribeInfo;
import org.bazinga.common.protocol.BazingaProtocol;
import org.bazinga.common.utils.NamedThreadFactory;
import org.bazinga.common.utils.NativeSupport;

/**
 * 消费端向monitor端注册所需的服务
 * @author BazingaLyn
 *
 * @time
 */
public abstract class DefaultConsumerRegistry extends AbstractCommonClient implements Registry {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultConsumerRegistry.class);
	
	/****订阅的服务名*****/
	private SubScribeInfo info;

	private MessageEncoder messageEncoder = new MessageEncoder();

	private ConsumerRegistryHandler consumerHandler = new ConsumerRegistryHandler();
	
	private final AcknowledgeEncoder ackEncoder = new AcknowledgeEncoder();

	private Bootstrap bootstrap;

	private EventLoopGroup worker;
	
	private int nWorkers;

	private final boolean nativeEt;

	protected volatile ByteBufAllocator allocator;

	protected final HashedWheelTimer timer = new HashedWheelTimer(new NamedThreadFactory("consumer.registry.timer"));

	public static final int WRITER_IDLE_TIME_SECONDS = 30;

	private final ConnectorIdleStateTrigger idleStateTrigger = new ConnectorIdleStateTrigger();

	public DefaultConsumerRegistry(SubScribeInfo info) {
		this.info = info;
		nativeEt = true;
		init();
	}

	private void init() {
		ThreadFactory workerFactory = new DefaultThreadFactory("baiznga.connector");
		worker = initEventLoopGroup(nWorkers, workerFactory);

		bootstrap = new Bootstrap().group(worker);

		if (worker instanceof EpollEventLoopGroup) {
			((EpollEventLoopGroup) worker).setIoRatio(100);
		} else if (worker instanceof NioEventLoopGroup) {
			((NioEventLoopGroup) worker).setIoRatio(100);
		}

		bootstrap.option(ChannelOption.ALLOCATOR, allocator).option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT)
				.option(ChannelOption.SO_REUSEADDR, true).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) SECONDS.toMillis(3))
				.channel(NioSocketChannel.class);

		bootstrap.option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.ALLOW_HALF_CLOSURE, false);
	}

	private EventLoopGroup initEventLoopGroup(int nWorkers, ThreadFactory workerFactory) {
		return isNativeEt() ? new EpollEventLoopGroup(nWorkers, workerFactory) : new NioEventLoopGroup(nWorkers, workerFactory);
	}

	private boolean isNativeEt() {
		return nativeEt && NativeSupport.isSupportNativeET();
	}

	public void connectToRegistryServer(int port, String host) throws Exception {
		

		final Bootstrap boot = bootstrap();

		final SocketAddress socketAddress = InetSocketAddress.createUnresolved(host, port);

		final ConnectionWatchdog watchdog = new ConnectionWatchdog(boot, timer, socketAddress) {

			public ChannelHandler[] handlers() {
				return new ChannelHandler[] { 
						this, 
						new IdleStateChecker(timer, 0, WRITER_IDLE_TIME_SECONDS, 0),
						idleStateTrigger,
						messageEncoder,
						ackEncoder,
						new MessageDecoder(), 
						consumerHandler };
			}
		};
		watchdog.setReconnect(true);
		ChannelFuture future;
		try {
			synchronized (bootstrap) {
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

	private Bootstrap bootstrap() {
		return bootstrap;
	}

	@ChannelHandler.Sharable
	static class MessageEncoder extends MessageToByteEncoder<Message> {

		@Override
		protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
			byte[] bytes = serializerImpl().writeObject(msg);

			out.writeShort(MAGIC).writeByte(msg.sign()).writeByte(0).writeLong(0).writeInt(bytes.length).writeBytes(bytes);
		}
	}

	static class MessageDecoder extends ReplayingDecoder<MessageDecoder.State> {

		/**
		 * 为state()方法中的值赋值
		 */
		public MessageDecoder() {
			super(State.HEADER_MAGIC);
		}

		private final BazingaProtocol header = new BazingaProtocol();

		@Override
		protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

			logger.info("/*********************begin decoder****************/");
			switch (state()) {

			case HEADER_MAGIC:
				checkMagic(in.readShort()); // MAGIC
				checkpoint(State.HEADER_SIGN);
			case HEADER_SIGN:
				header.sign(in.readByte()); // 消息标志位
				checkpoint(State.HEADER_STATUS);
			case HEADER_STATUS:
				in.readByte(); // no-op
				checkpoint(State.HEADER_ID);
			case HEADER_ID:
				header.id(in.readLong()); // 消息id
				checkpoint(State.HEADER_BODY_LENGTH);
			case HEADER_BODY_LENGTH:
				header.bodyLength(in.readInt()); // 消息体长度
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
			HEADER_MAGIC, HEADER_SIGN, HEADER_STATUS, HEADER_ID, HEADER_BODY_LENGTH, BODY
		}
	}

	class ConsumerRegistryHandler extends ChannelInboundHandlerAdapter {

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {

			logger.debug("***********consumer has start**************");
			logger.info("consumer msg :{}", info);

			Channel channel = ctx.channel();

			Message message = new Message();

			message.sign(SUBSCRIBE_SERVICE);
			message.data(info);

			channel.writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

			logger.error("consumer Handler occur exception:{}", cause.getMessage());

			ctx.channel().close();
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

			Channel _channel = ctx.channel();
			
			logger.info("comsume received some message from monitor {}", msg);
			if (msg instanceof Message) {
				Message message = (Message) msg;

				switch (message.sign()) {
				case PUBLISH_SERVICE:

					if (message.data() instanceof ProviderInfos) {

						ProviderInfos infos = (ProviderInfos) message.data();

						List<ProviderInfo> list = infos.getProviders();
						String serviceName = infos.getServiceName();
						
						logger.info("get service {}",serviceName);

						for (ProviderInfo info : list) {
							Channel channel = connectToProvider(info.getAddress().getPort(), info.getAddress().getHost());
							if (null == channel) {
								logger.warn("port {} and host {} connection failed.", info.getAddress().getPort(), info.getAddress().getHost());
								continue;
							}
							addInfo(serviceName, channel, info.getWeight());
						}
						
						ConectionPreHeater.finishPreConnection(serviceName);
						
						logger.info("receive monitor provider info and will send ACK");
						_channel.writeAndFlush(new Acknowledge(message.sequence())).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);;
					}
					break;
				case OFFLINE_NOTICE:
					break;
				default:
					break;
				}

			} 
		}
	}

	protected abstract Channel connectToProvider(int port, String host);
}
