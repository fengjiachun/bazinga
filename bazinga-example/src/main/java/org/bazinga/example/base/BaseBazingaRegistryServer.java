package org.bazinga.example.base;

import java.net.InetSocketAddress;

import org.bazinga.monitor.MonitorServer;
import org.bazinga.registry.RegistryServer;


public class BaseBazingaRegistryServer {
	
	public static void main(String[] args) {
		
		
		 RegistryServer registryServer = RegistryServer.Default.createRegistryServer(20001, 1);
		 MonitorServer monitor = new MonitorServer(new InetSocketAddress(19998));
		 monitor.setRegistryMonitor(registryServer);
		 
		 registryServer.startRegistryServer();
		 
		 
	}

}
