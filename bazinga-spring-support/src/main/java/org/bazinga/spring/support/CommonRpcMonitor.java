package org.bazinga.spring.support;

import org.bazinga.monitor.BazingaMonitor;
import org.springframework.beans.factory.InitializingBean;

/**
 * 
 * @author BazingaLyn
 * 说明Monitor端对spring的支持
 * @time
 */
public class CommonRpcMonitor implements InitializingBean {
	
	private int port;
	
	private BazingaMonitor bazingaMonitor;

	public void afterPropertiesSet() throws Exception {
		bazingaMonitor = new BazingaMonitor(port);
		bazingaMonitor.start(false);
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setBazingaMonitor(BazingaMonitor bazingaMonitor) {
		this.bazingaMonitor = bazingaMonitor;
	}

	public BazingaMonitor getBazingaMonitor() {
		return bazingaMonitor;
	}

}
