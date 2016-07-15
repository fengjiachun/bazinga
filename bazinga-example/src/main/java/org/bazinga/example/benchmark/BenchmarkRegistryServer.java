package org.bazinga.example.benchmark;

import java.net.InetSocketAddress;

import org.bazinga.monitor.MonitorServer;
import org.bazinga.registry.RegistryServer;

public class BenchmarkRegistryServer {
	
//	private static final InternalLogger logger = InternalLoggerFactory.getInstance(BenchmarkRegistryServer.class);

	
	public static void main(String[] args) throws Exception {
		
		RegistryServer registryServer = RegistryServer.Default.createRegistryServer(8080, 1);
		 MonitorServer monitor = new MonitorServer(new InetSocketAddress(19998));
		 monitor.setRegistryMonitor(registryServer);
		 
		 registryServer.startRegistryServer();
		
	}
	

}
