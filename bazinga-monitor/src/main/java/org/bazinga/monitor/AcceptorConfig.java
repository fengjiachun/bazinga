package org.bazinga.monitor;

public interface AcceptorConfig {
	
	void start() throws InterruptedException;
	
	void shutdownGracefully();

}
