package org.bazinga.monitor;

import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;

import java.net.SocketAddress;
import java.util.concurrent.ThreadFactory;

import org.bazinga.common.transport.netty.NettyAcceptor;
import org.bazinga.registry.RegistryMonitor;
/**
 * 
 * @author BazingaLyn
 * TODO monitor端未做
 * @time 2016年7月12日
 */
public class MonitorServer extends NettyAcceptor {
	
	private volatile RegistryMonitor registryMonitor;
	
	public void setRegistryMonitor(RegistryMonitor registryMonitor) {
        this.registryMonitor = registryMonitor;
    }

	public MonitorServer(SocketAddress localAddress) {
		super(localAddress);
	}

	@Override
	protected EventLoopGroup initEventLoopGroup(int worker, ThreadFactory bossFactory) {
		return null;
	}

	@Override
	protected ChannelFuture bind(SocketAddress localAddress) {
		return null;
	}

}
