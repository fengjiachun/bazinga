package org.bazinga.client.provider;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.bazinga.common.protocol.BazingaProtocol.MAGIC;
import static org.bazinga.common.protocol.BazingaProtocol.PUBLISH_SERVICE;
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
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ThreadFactory;

import org.bazinga.client.Registry;
import org.bazinga.client.trigger.ConnectorIdleStateTrigger;
import org.bazinga.client.watch.ConnectionWatchdog;
import org.bazinga.common.exception.ConnectFailedException;
import org.bazinga.common.idle.IdleStateChecker;
import org.bazinga.common.message.Message;
import org.bazinga.common.message.RegistryInfo;
import org.bazinga.common.utils.NamedThreadFactory;
import org.bazinga.common.utils.NativeSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * provider端代码
 */
public class DefaultProviderRegistry implements Registry {

	protected static final Logger logger = LoggerFactory.getLogger(DefaultProviderRegistry.class);

	private MessageEncoder messageEncoder = new MessageEncoder();

	private ProviderRegistryHandler provider = new ProviderRegistryHandler();

	private RegistryInfo info;

	private Bootstrap bootstrap;

	private EventLoopGroup worker;
	private int nWorkers;

	private final boolean nativeEt;

	protected volatile ByteBufAllocator allocator;

	protected final HashedWheelTimer timer = new HashedWheelTimer(new NamedThreadFactory("consumer.registry.timer"));

	public static final int WRITER_IDLE_TIME_SECONDS = 30;

	private final ConnectorIdleStateTrigger idleStateTrigger = new ConnectorIdleStateTrigger();

	public DefaultProviderRegistry(RegistryInfo info) {
		this.info = info;
		this.nativeEt = true;
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

			// TODO LOSS ackHandler
			public ChannelHandler[] handlers() {
				return new ChannelHandler[] { 
						this, 
						new IdleStateChecker(timer, 0, WRITER_IDLE_TIME_SECONDS, 0), 
						idleStateTrigger,
						messageEncoder,
						provider };
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

	class ProviderRegistryHandler extends ChannelInboundHandlerAdapter {

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
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			logger.error("occur exception:{}", cause.getMessage());
			ctx.channel().close();
		}
		
		
	}

	public void providerRegistryInfo(RegistryInfo registryInfo) {
		this.info = registryInfo;
	}

}
