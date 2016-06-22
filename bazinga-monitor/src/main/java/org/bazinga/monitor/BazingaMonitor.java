package org.bazinga.monitor;

import static org.bazinga.common.protocol.BazingaProtocol.ACK;
import static org.bazinga.common.protocol.BazingaProtocol.MAGIC;
import static org.bazinga.common.protocol.BazingaProtocol.OFFLINE_NOTICE;
import static org.bazinga.common.protocol.BazingaProtocol.PUBLISH_SERVICE;
import static org.bazinga.common.protocol.BazingaProtocol.SUBSCRIBE_SERVICE;
import static org.bazinga.common.serialization.SerializerHolder.serializerImpl;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.bazinga.common.ack.AcknowledgeEncoder;
import org.bazinga.common.exception.BazingaException;
import org.bazinga.common.idle.IdleStateChecker;
import org.bazinga.common.message.Acknowledge;
import org.bazinga.common.message.Message;
import org.bazinga.common.message.ProviderInfo;
import org.bazinga.common.message.ProviderInfos;
import org.bazinga.common.message.RegistryInfo;
import org.bazinga.common.message.RegistryInfo.Address;
import org.bazinga.common.message.RegistryInfo.RpcService;
import org.bazinga.common.message.SubScribeInfo;
import org.bazinga.common.protocol.BazingaProtocol;
import org.bazinga.common.trigger.AcceptorIdleStateTrigger;
import org.bazinga.common.utils.NamedThreadFactory;
import org.bazinga.common.utils.NativeSupport;
import org.bazinga.common.utils.SystemClock;
import org.bazinga.monitor.registryInfo.RegistryContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
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
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelMatcher;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.internal.ConcurrentSet;

/**
 * monitor端
 * @author Lyncc
 *
 */
public class BazingaMonitor extends DefaultMonitorConfig {
	
	protected static final Logger logger = LoggerFactory.getLogger(BazingaMonitor.class); 
	
	private final boolean nativeEt;
	
	private MessageHandler messageHandler = new MessageHandler();
	
	private MessageEncoder messageEncoder = new MessageEncoder();
	
	private ChannelGroup subscribeChannels = new DefaultChannelGroup("subscribers", GlobalEventExecutor.INSTANCE);
	
	private RegistryContext registryContext = new RegistryContext();
	
	private final ConcurrentMap<String, MessageNonAck> messagesNonAck = new ConcurrentHashMap<String, MessageNonAck>();
	
	public static final AttributeKey<ConcurrentSet<String>> NETTY_CHANNEL_SUBSCRIBERS = AttributeKey.valueOf("netty.channel.subscribers");
	
	public static final AttributeKey<RegistryInfo> NETTY_CHANNEL_PUBLISH = AttributeKey.valueOf("netty.channel.publish");
	
	protected final HashedWheelTimer timer = new HashedWheelTimer(new NamedThreadFactory("monitor.timer"));
	
	private final AcceptorIdleStateTrigger idleStateTrigger = new AcceptorIdleStateTrigger();
	
	private final AcknowledgeEncoder ackEncoder = new AcknowledgeEncoder();
	
	public static final int READER_IDLE_TIME_SECONDS =  60;
	
	
	public BazingaMonitor(int port) {
		super(new InetSocketAddress(port));
		nativeEt = true;
		init();
    }
	
	@ChannelHandler.Sharable
    class MessageHandler extends ChannelInboundHandlerAdapter {
		
		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			logger.info("channel has actived");
		}
		
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			Channel channel  = ctx.channel();
			logger.info("receive message from address :{}",channel.remoteAddress());
			if(msg instanceof Message){
				Message message = (Message)msg;
				
				switch (message.sign()) {
				case PUBLISH_SERVICE:
					
					RegistryInfo registryInfo = (RegistryInfo)message.data();
					handlerPublishService(channel,registryInfo);
					// 接收到发布信息的时候，要给发布者回复ACK
					channel.writeAndFlush(new Acknowledge(message.sequence())).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
					break;
				case SUBSCRIBE_SERVICE:
					SubScribeInfo subScribeInfo = (SubScribeInfo)message.data();
					handlerSubscribeService(channel,subScribeInfo);
					channel.writeAndFlush(new Acknowledge(message.sequence())).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
					break;
					
				default:
					break;
				}
			}else if(msg instanceof Acknowledge){
				handlerAcknowledge((Acknowledge)msg,channel);
			}
		}
		
		@Override
		public void channelWritabilityChanged(ChannelHandlerContext ctx)
				throws Exception {
			Channel channel  = ctx.channel();
			
			if(!channel.isWritable()){
				
				logger.warn("{} is not writable, high water mask: {}, the number of flushed entries that are not written yet: {}.",channel,channel.config().getWriteBufferHighWaterMark(), channel.unsafe().outboundBuffer().size());
				channel.config().setAutoRead(false);
			}else {
                // 曾经高于高水位线的OutboundBuffer现在已经低于WRITE_BUFFER_LOW_WATER_MARK了
                logger.warn("{} is writable(rehabilitate), low water mask: {}, the number of flushed entries that are not written yet: {}.",
                		channel, channel.config().getWriteBufferLowWaterMark(), channel.unsafe().outboundBuffer().size());

                channel.config().setAutoRead(true);
            }
		}
		
		
		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			Channel channel  = ctx.channel();
			
			
			
			Attribute<RegistryInfo> attr = channel.attr(NETTY_CHANNEL_PUBLISH);
			RegistryInfo registryInfo = attr.get();
			if(null == registryInfo){
				return;
			}
			registryContext.removeRegistryInfo(registryInfo);
			
			Address address = registryInfo.getAddress();
			handleOfflineNotice(address);
		}
		

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
				throws Exception {
			logger.error("发生了异常:{}",cause.getMessage());
			ctx.channel().close();
		}
		
	}
	
	/**
	 * 处理客户端发送过来的ack信息
	 * 将发过来的ack信息从{@messagesNonAck}这个对象中移除
	 * @param msg
	 * @param channel
	 */
	private void handlerAcknowledge(Acknowledge msg, Channel channel) {
		messagesNonAck.remove(key(msg.sequence(), channel));
	}
     
	/**
	 * 通知订阅者某地址上所有的服务下线
	 * TODO @problem 这里需要优化，因为有些订阅者并不关注某些地址是否上下线，这样会引起羊群效应
	 * @param address
	 */
    private void handleOfflineNotice(Address address) {
			
    	logger.info("notify consumer address {} offine",address);
    	
    	Message msg = new Message();
        msg.sign(OFFLINE_NOTICE);
        msg.data(address);
        subscribeChannels.writeAndFlush(msg);
	}
	
    /**
     * 处理订阅者的服务
     * 要做的事情有：
     * 1：将订阅者的channel的attr上绑定该channel订阅的service信息，@reason:这样可以是当提供该服务的提供者上线注册的时候，可以在订阅者的
     * channelGroup中找到需要消费这种服务的channel发送信息
     * 2：将已经提供该服务的服务器的地址信息发送给订阅者
     * @param channel
     * @param subScribeInfo
     */
	private void handlerSubscribeService(Channel channel,SubScribeInfo subScribeInfo) {
		
		logger.info("subscribe info from {} and info is {}",channel.remoteAddress().toString(),subScribeInfo.toString());
		
		//订阅的内容不为空
		if(null != subScribeInfo.getServiceNames() && !subScribeInfo.getServiceNames().isEmpty()){
			
			//将订阅的信息绑定在channel的attribute属性上
			attachSubscribeEventOnChannel(channel,subScribeInfo);
			
			//
			subscribeChannels.add(channel);
			
			for(String eachServiceName:subScribeInfo.getServiceNames()){
				
				ConcurrentMap<Address, Integer>  providerInfos = registryContext.getProviderInfoByServiceName(eachServiceName);
				
				if(null == providerInfos || providerInfos.isEmpty()){
					
					logger.warn("now this service {} has no hosts which provider this service",eachServiceName);
					continue;
				}
				
				ProviderInfos providerInfosList = createRpcService(providerInfos,eachServiceName); 
				final Message msg = new Message();
		        msg.sign(PUBLISH_SERVICE);
		        msg.data(providerInfosList);
		        
		        MessageNonAck msgNonAck = new MessageNonAck(eachServiceName, msg, channel);
		        // 收到ack后会移除当前key(参见handleAcknowledge), 否则超时超时重发
		        messagesNonAck.put(msgNonAck.id, msgNonAck);
		        
		        channel.writeAndFlush(msg);
				
			}
			
			
		}
		
	}

	/**
	 * 将提供者的map信息封装成Entity方便传输
	 * @param providerInfos
	 * @param eachServiceName 
	 * @return
	 */
	private ProviderInfos createRpcService(ConcurrentMap<Address, Integer> providerInfos, String eachServiceName) {
		
		List<ProviderInfo> providerInfoLists = new ArrayList<ProviderInfo>();
		
		Set<Entry<Address, Integer>> entries = providerInfos.entrySet();
		
		for(Entry<Address, Integer> obj:entries){
			ProviderInfo providerInfo = new ProviderInfo(obj.getKey(), obj.getValue());
			providerInfoLists.add(providerInfo);
		}
		
		return new ProviderInfos(eachServiceName,providerInfoLists);
	}

	/**
	 * 将订阅信息绑定到channel的attribute上
	 * @param channel
	 * @param subScribeInfo
	 * @return
	 */
	private static boolean attachSubscribeEventOnChannel(Channel channel,SubScribeInfo subScribeInfo) {
		
		List<String> serviceNames = subScribeInfo.getServiceNames();
		
		Attribute<ConcurrentSet<String>>  attr = channel.attr(NETTY_CHANNEL_SUBSCRIBERS);
		
		ConcurrentSet<String> existSerivceNames = attr.get();
		
		if (existSerivceNames == null) {
			
            ConcurrentSet<String> newServiceNamesSet = new ConcurrentSet<String>();
            existSerivceNames = attr.setIfAbsent(newServiceNamesSet);
            if (existSerivceNames == null) {
            	existSerivceNames = newServiceNamesSet;
            }
        }

        return existSerivceNames.addAll(serviceNames);
		
	}

	/**
	 * 将发布信息绑定到发布者的channel上
	 * @param channel
	 * @param registryInfo
	 */
	private void handlerPublishService(Channel channel,RegistryInfo registryInfo) {
		
		logger.info("Publish service {} from channel {}",registryInfo,channel);
		
		attachPublishEventOnChannel(registryInfo, channel);

		registryContext.registryCurrentInfo(registryInfo);
		
		List<RpcService> rpcServices = registryInfo.getRpcServices();
		
		if(null != rpcServices && !rpcServices.isEmpty()){
			
			for(RpcService rpcService : rpcServices){
				
				final String serviceName = rpcService.getServiceName();
				
				ConcurrentMap<Address, Integer> providerInfos = registryContext.getProviderInfoByServiceName(serviceName);
				
				ProviderInfos providerInfosList = createRpcService(providerInfos,serviceName);
				
				final Message msg = new Message();
                msg.sign(PUBLISH_SERVICE);
                msg.data(providerInfosList);
                
                subscribeChannels.writeAndFlush(msg, new ChannelMatcher() {

                    public boolean matches(Channel channel) {
                        boolean doSend = isChannelSubscribeOnServiceName(serviceName, channel);
                        if (doSend) {
                            MessageNonAck msgNonAck = new MessageNonAck(serviceName, msg, channel);
                            // 收到ack后会移除当前key(参见handleAcknowledge), 否则超时超时重发
                            messagesNonAck.put(msgNonAck.id, msgNonAck);
                        }
                        return doSend;
                    }
                });
				
			}
		}
		
	}
	
	private static boolean isChannelSubscribeOnServiceName(String serviceName, Channel channel) {
		
		ConcurrentSet<String> container = channel.attr(NETTY_CHANNEL_SUBSCRIBERS).get();
		return container != null && container.contains(serviceName);
		
	}
	
	private void attachPublishEventOnChannel(RegistryInfo registryInfo,Channel channel) {
		
		Attribute<RegistryInfo> attr = channel.attr(NETTY_CHANNEL_PUBLISH);
		
		RegistryInfo existInfo = attr.get();
		
		if(null == existInfo){
			attr.setIfAbsent(registryInfo);
		}
	}

	@ChannelHandler.Sharable
    static class MessageEncoder extends MessageToByteEncoder<Message> {

        @Override
        protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
            byte[] bytes = serializerImpl().writeObject(msg);

            out.writeShort(MAGIC) // MAGIC
                    .writeByte(msg.sign()) //消息标志位
                    .writeByte(0) // no-op
                    .writeLong(0) // 消息id
                    .writeInt(bytes.length) // 消息体长度
                    .writeBytes(bytes); //消息体
        }
    }
	
	static class MessageNonAck {
		
		private final String id;
		private final String serviceName;
        private final Message msg;
        private final Channel channel;
        private final long timestamp = SystemClock.millisClock().now();

        public MessageNonAck(String serviceName, Message msg, Channel channel) {
            this.serviceName = serviceName;
            this.msg = msg;
            this.channel = channel;

            id = key(msg.sequence(), channel);
        }

		@Override
		public String toString() {
			return "MessageNonAck [id=" + id + ", serviceName=" + serviceName
					+ ", msg=" + msg + ", channel=" + channel + ", timestamp="
					+ timestamp + "]";
		}
        
	}
	
	private static String key(long sequence, Channel channel) {
		return String.valueOf(sequence) + '-' + channel.id().asShortText();
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
		protected void decode(ChannelHandlerContext ctx, ByteBuf in,
				List<Object> out) throws Exception {
			
			logger.info("/*********************begin decoder****************/");
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
            	logger.info("/***************************{}******/",header.sign());
                switch (header.sign()) {
                
                    case PUBLISH_SERVICE:
                    case SUBSCRIBE_SERVICE:
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
				logger.error("Magic is not match");
                throw new BazingaException("magic value is not equal "+MAGIC);
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
	
	public RegistryContext getRegistryContext() {
		return registryContext;
	}

	public void setRegistryContext(RegistryContext registryContext) {
		this.registryContext = registryContext;
	}

	private class AckTimeoutScanner implements Runnable {
		
		public void run() {
			for(;;){
				try {
					
					
					for(MessageNonAck mna:messagesNonAck.values()){
						//查看10秒内没有ack的信息，进行重发
						if(SystemClock.millisClock().now() - mna.timestamp > TimeUnit.SECONDS.toMillis(10)){
							
							if (messagesNonAck.remove(mna.id) == null) {
                                continue;
                            }
							
							
							if (mna.channel.isActive()) {
								logger.warn("有信息超时");
								logger.warn("message {} send failed",mna);
                                MessageNonAck msgNonAck = new MessageNonAck(mna.serviceName, mna.msg, mna.channel);
                                messagesNonAck.put(msgNonAck.id, msgNonAck);
                                mna.channel.writeAndFlush(mna.msg)
                                        .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
                            }
						}
					}
					Thread.sleep(900);
				} catch (Exception e) {
					logger.error("when loop ack message occur exception:{}",e.getMessage());
				}
			}
		}
	 }
	
	{
		Thread t = new Thread(new AckTimeoutScanner(), "ack.timeout.scanner");
        t.setDaemon(true);
        t.start();
	}

	@Override
	protected EventLoopGroup initEventLoopGroup(int workers,ThreadFactory bossFactory) {
		return isNativeEt() ? new EpollEventLoopGroup(workers, bossFactory) : new NioEventLoopGroup(workers, bossFactory);
	}

	private boolean isNativeEt() {
		return nativeEt && NativeSupport.isSupportNativeET();
	}

	@Override
	protected ChannelFuture bind(SocketAddress localAddress) {
		ServerBootstrap boot = bootstrap();

        if (isNativeEt()) {
            boot.channel(EpollServerSocketChannel.class);
        } else {
            boot.channel(NioServerSocketChannel.class);
        }
        boot.childHandler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
            	ch.pipeline().addLast(
            			new IdleStateChecker(timer, READER_IDLE_TIME_SECONDS, 0, 0),
            			idleStateTrigger
            			,messageEncoder
            			,ackEncoder
            			,new MessageDecoder()
            			,messageHandler);
            }
        });

        setOptions();

        return boot.bind(localAddress);
	}

	private void setOptions() {
		ServerBootstrap boot = bootstrap();

        // parent options
        boot.option(ChannelOption.SO_BACKLOG, 1024);
        boot.option(ChannelOption.SO_REUSEADDR, true);

        // child options
        boot.childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.ALLOW_HALF_CLOSURE, false);
	}

}
