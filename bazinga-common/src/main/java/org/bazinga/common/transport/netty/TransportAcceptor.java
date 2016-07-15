package org.bazinga.common.transport.netty;

import java.net.SocketAddress;

/**
 * 传输数据层的接收端抽象
 * @author BazingaLyn
 * @copyright fjc
 * @time 2016年7月14日
 */
public interface TransportAcceptor {
	
	SocketAddress localAddress();
	
	void start() throws InterruptedException;
	
	void start(boolean sync) throws InterruptedException;
	
	void shutdownGracefully();

}
