package org.bazinga.common.transport.netty;

import static org.bazinga.common.utils.Constants.AVAILABLE_PROCESSORS;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMessageSizeEstimator;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.internal.PlatformDependent;

import java.net.SocketAddress;
import java.util.concurrent.ThreadFactory;

import org.bazinga.common.logger.InternalLogger;
import org.bazinga.common.logger.InternalLoggerFactory;
import org.bazinga.common.utils.NamedThreadFactory;


/**
 * netty的server端，不管是monitor/registry/consumer/provider端只要在C/S模型中作为Server端
 * 可以继承此类，减少server模型中的重复代码
 * @author BazingaLyncc
 * @copyright fjc
 * @modifytime 2016年7月14日22:48:48
 */
public abstract class NettyAcceptor implements TransportAcceptor {
	
	private static final InternalLogger logger = InternalLoggerFactory.getInstance(NettyAcceptor.class);
	
	private ServerBootstrap bootstrap;
	protected final SocketAddress localAddress;
	
	protected final HashedWheelTimer timer = new HashedWheelTimer(new NamedThreadFactory("netty.acceptor.timer"));
	
	private EventLoopGroup boss;
    private EventLoopGroup worker;
    
    private int nWorkers;
    
    protected volatile ByteBufAllocator allocator;
    
    public NettyAcceptor(SocketAddress localAddress) {
        this(localAddress, AVAILABLE_PROCESSORS << 1);
    }
    
    public NettyAcceptor(SocketAddress localAddress, int nWorkers) {
        this.localAddress = localAddress;
        this.nWorkers = nWorkers;
    }
    
    //默认的一些公用的netty的配置参数或者一些线程配置
    //子类可以根据自己的一些需求重新配置
    protected void init() {
    	ThreadFactory bossFactory = new DefaultThreadFactory("bazinga.monitor.acceptor.boss");
        ThreadFactory workerFactory = new DefaultThreadFactory("bazinga.monitor.acceptor.worker");
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
    }

	protected abstract EventLoopGroup initEventLoopGroup(int worker, ThreadFactory bossFactory);

	@Override
	public SocketAddress localAddress() {
		return localAddress;
	}

	@Override
	public void start() throws InterruptedException {
		this.start(true);
	}

	@Override
	public void start(boolean sync) throws InterruptedException {
		ChannelFuture future = bind(localAddress).sync();

        logger.info("Bazinga acceptor server start");

        if (sync) {
            future.channel().closeFuture().sync();
        }
	}
	
	protected ServerBootstrap bootstrap() {
        return bootstrap;
    }

	protected abstract ChannelFuture bind(SocketAddress localAddress);

	@Override
	public void shutdownGracefully() {
		boss.shutdownGracefully().awaitUninterruptibly();
        worker.shutdownGracefully().awaitUninterruptibly();
	}

}
