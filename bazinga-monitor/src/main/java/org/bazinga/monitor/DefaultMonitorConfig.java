package org.bazinga.monitor;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMessageSizeEstimator;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.internal.PlatformDependent;

import java.net.SocketAddress;
import java.util.concurrent.ThreadFactory;

import org.bazinga.common.logger.InternalLogger;
import org.bazinga.common.logger.InternalLoggerFactory;
import org.bazinga.monitor.registryInfo.RegistryContext;

import static org.bazinga.common.utils.Constants.AVAILABLE_PROCESSORS;

/**
 * 
 * @author BazingaLyn
 * @copyright fjc
 * @time
 */
public abstract class DefaultMonitorConfig implements AcceptorConfig {
	
	private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultMonitorConfig.class);
	
	protected RegistryContext registryContext = new RegistryContext();
	
	private ServerBootstrap bootstrap;
	protected final SocketAddress localAddress;
	
	private EventLoopGroup boss;
    private EventLoopGroup worker;
    
    private int nWorkers;
    
    protected volatile ByteBufAllocator allocator;
    
    public DefaultMonitorConfig(SocketAddress localAddress) {
    	 this(localAddress, AVAILABLE_PROCESSORS << 1);
	}
    
    public DefaultMonitorConfig(SocketAddress localAddress, int nWorkers) {
        this.localAddress = localAddress;
        this.nWorkers = nWorkers;
    }
    
    protected void init(){
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
    
    @Override
    public void start() throws InterruptedException {
    	
    	start(true);
    }
	
	public void start(boolean sync) throws InterruptedException {
		ChannelFuture future = bind(localAddress).sync();
		
		 logger.info("监控端即将启动~");
		 
		 if (sync) {
	            // Wait until the server socket is closed.
	            future.channel().closeFuture().sync();
	      }
		
	}
	
	protected ServerBootstrap bootstrap() {
        return bootstrap;
    }

	public void shutdownGracefully() {
		 boss.shutdownGracefully().awaitUninterruptibly();
	     worker.shutdownGracefully().awaitUninterruptibly();
	}
	
	protected abstract ChannelFuture bind(SocketAddress localAddress);
	
	protected abstract EventLoopGroup initEventLoopGroup(int workers, ThreadFactory bossFactory);

}
