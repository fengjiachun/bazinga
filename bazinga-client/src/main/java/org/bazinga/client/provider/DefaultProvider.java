package org.bazinga.client.provider;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMessageSizeEstimator;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.internal.PlatformDependent;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;

import org.bazinga.client.decoder.ProviderDecoder;
import org.bazinga.client.encoder.ResponseEncoder;
import org.bazinga.client.handler.ProviderHandler;
import org.bazinga.client.processor.provider.DefaultProviderProcessor;
import org.bazinga.client.provider.model.ServiceWrapper;
import org.bazinga.common.idle.IdleStateChecker;
import org.bazinga.common.logger.InternalLogger;
import org.bazinga.common.logger.InternalLoggerFactory;
import org.bazinga.common.message.RegistryInfo;
import org.bazinga.common.message.RegistryInfo.Address;
import org.bazinga.common.trigger.AcceptorIdleStateTrigger;
import org.bazinga.common.utils.NativeSupport;

/**
 * 服务的提供者，从Netty的server/client角度上来说
 * 此类是server 用来处理消费者发送的服务请求
 * @author BazingaLyn
 *
 * @time
 */
public class DefaultProvider extends DefaultProviderRegistry {
	
	private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultProvider.class);
	
	/***在哪个端口上提供服务，这个端口号需要发送给monitor,这样monitor才会把该信息发送给client****/
	private int providerPort;
	
	/*****响应的编码器*****/
	private ResponseEncoder encoder = new ResponseEncoder();
	
	private ProviderHandler handler = new ProviderHandler(new DefaultProviderProcessor(this));
	
	private final AcceptorIdleStateTrigger idleStateTrigger = new AcceptorIdleStateTrigger();
	
	private final ServiceProviderContainer providerContainer = new DefaultServiceProviderContainer();
	
	private ServerBootstrap bootstrap;
	
	private EventLoopGroup boss;
    private EventLoopGroup worker;
    
    private final boolean nativeEt;
    
    private int nWorkers;
    
    public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
    
    public static final int READER_IDLE_TIME_SECONDS =  60;
    
    protected volatile ByteBufAllocator allocator;
    
    private volatile int writeBufferHighWaterMark = -1;
    private volatile int writeBufferLowWaterMark = -1;
	

	public DefaultProvider(RegistryInfo info) {
		super(info);
		this.providerPort = info.getAddress().getPort();
		this.nWorkers = AVAILABLE_PROCESSORS << 1;
		this.nativeEt = true;
		doInit();
	}
	
	public DefaultProvider(Address address,List<ServiceWrapper> serviceWrappers,int writeBufferHighWaterMark,int writeBufferLowWaterMark){
		this(transform(address, serviceWrappers));
		registryService(serviceWrappers);
		this.writeBufferHighWaterMark = writeBufferHighWaterMark;
		this.writeBufferLowWaterMark = writeBufferLowWaterMark;
	}
	
	public DefaultProvider(Address address,List<ServiceWrapper> serviceWrappers){
		this(address, serviceWrappers, -1, -1);
	}
	
	private void registryService(List<ServiceWrapper> serviceWrappers) {
		
		if(null == serviceWrappers || serviceWrappers.isEmpty()){
			return;
		}
		for(ServiceWrapper serviceWrapper : serviceWrappers){
			providerContainer.registerService(serviceWrapper.getServiceName(), serviceWrapper);
		}
	}

	public static RegistryInfo transform(Address address,List<ServiceWrapper> serviceWrappers){
		if(null == serviceWrappers || serviceWrappers.isEmpty()){
			return null;
		}
		RegistryInfo registryInfo = new RegistryInfo();
		registryInfo.setAddress(address);
		List<org.bazinga.common.message.RegistryInfo.RpcService> rpcSerivces = new ArrayList<org.bazinga.common.message.RegistryInfo.RpcService>();
		for(ServiceWrapper serviceWrapper :serviceWrappers){
			org.bazinga.common.message.RegistryInfo.RpcService rpcService = new org.bazinga.common.message.RegistryInfo.RpcService(serviceWrapper.getServiceName(),serviceWrapper.getWeight(),serviceWrapper.getAppName(),serviceWrapper.getResponsiblityName());
			rpcSerivces.add(rpcService);
		}
		registryInfo.setRpcServices(rpcSerivces);
		return registryInfo;
	}
	
	private void doInit() {
		ThreadFactory bossFactory = new DefaultThreadFactory("bazinga.acceptor.boss");
        ThreadFactory workerFactory = new DefaultThreadFactory("bazinga.acceptor.worker");
        boss = initEventLoopGroup(1, bossFactory);
        worker = initEventLoopGroup(nWorkers, workerFactory);
        bootstrap = new ServerBootstrap().group(boss, worker);
        //使用池化的directBuffer
        allocator = new PooledByteBufAllocator(PlatformDependent.directBufferPreferred());
        
        bootstrap.childOption(ChannelOption.ALLOCATOR, allocator)
        .childOption(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT);
        
        if (boss instanceof EpollEventLoopGroup) {
            ((EpollEventLoopGroup) boss).setIoRatio(100);
        } else if (boss instanceof NioEventLoopGroup) {
            ((NioEventLoopGroup) boss).setIoRatio(100);
        }
        if (worker instanceof EpollEventLoopGroup) {
            ((EpollEventLoopGroup) worker).setIoRatio(100);
        } else if (worker instanceof NioEventLoopGroup) {
            ((NioEventLoopGroup) worker).setIoRatio(100);
        }
        
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        bootstrap.option(ChannelOption.SO_REUSEADDR, true);

        // child options
        bootstrap.childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.ALLOW_HALF_CLOSURE, false);
        
        if (writeBufferLowWaterMark >= 0 && writeBufferHighWaterMark > 0) {
            WriteBufferWaterMark waterMark = new WriteBufferWaterMark(writeBufferLowWaterMark, writeBufferHighWaterMark);
            bootstrap.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, waterMark);
        }
	}
	
	protected EventLoopGroup initEventLoopGroup(int workers,ThreadFactory bossFactory) {
		return isNativeEt() ? new EpollEventLoopGroup(workers, bossFactory) : new NioEventLoopGroup(workers, bossFactory);
	}

	private boolean isNativeEt() {
		return nativeEt && NativeSupport.isSupportNativeET();
	}

	public void start() throws InterruptedException{
        ChannelFuture future = bind(new InetSocketAddress(providerPort)).sync();
		
		logger.info("服务端即将启动服务~");
		
		 future.channel().closeFuture().sync();
	}

	private ChannelFuture bind(InetSocketAddress inetSocketAddress) {
		ServerBootstrap boot = bootstrap;

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
            			,new ProviderDecoder()
            			,encoder
            			,handler);
            }
        });
        return boot.bind(inetSocketAddress);
	}

	public ServiceProviderContainer getProviderContainer() {
		return providerContainer;
	}

}
