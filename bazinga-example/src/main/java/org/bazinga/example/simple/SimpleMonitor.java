package org.bazinga.example.simple;

import io.netty.util.internal.ConcurrentSet;

import java.util.Date;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.bazinga.client.comsumer.DefaultConsumer;
import org.bazinga.common.message.RegistryInfo.Address;
import org.bazinga.common.message.RegistryInfo.RpcService;
import org.bazinga.monitor.BazingaMonitor;
import org.bazinga.monitor.registryInfo.RegistryContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleMonitor {

	protected static final Logger logger = LoggerFactory
			.getLogger(DefaultConsumer.class);

	private static BazingaMonitor bazingaMonitor = null;

	public static void main(String[] args) throws InterruptedException {
		int port;
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		} else {
			port = 8080;
		}
		bazingaMonitor = new BazingaMonitor(port);
		Thread thread = new Thread(new MonitorScanner());
		thread.start();
		bazingaMonitor.start();
		
	}
	
	public static class MonitorScanner implements Runnable {

		public void run() {
			logger.info("/*******loop monitor begin**********/");
			
			for(;;){
				if(null == bazingaMonitor){
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					continue;
				}
				
				RegistryContext registryContext = bazingaMonitor.getRegistryContext();
				
				if(null != registryContext){
					
					ConcurrentMap<Address,ConcurrentSet<RpcService>> concurrentMap = registryContext.getGlobalInfo();
					
					if(concurrentMap.keySet().isEmpty()){
						logger.info("current time {} has no any address provider serivce",new Date());
					}else{
						Set<Entry<Address, ConcurrentSet<RpcService>>> entries = concurrentMap.entrySet();
						
						for(Entry<Address, ConcurrentSet<RpcService>> entry : entries){
							Address address = entry.getKey();
							ConcurrentSet<RpcService> servicesList = entry.getValue();
							for(RpcService rpcService :servicesList){
								logger.info("current time {} address is {} provider service {}",new Date(),address,rpcService.getServiceName());
							}
							
						}
					}
					
					
					ConcurrentMap<String, ConcurrentMap<Address, Integer>> serviceMaps = registryContext.getServiceInfo();
					
					if(serviceMaps.keySet().isEmpty()){
						logger.info("current time {} has no any serivce",new Date());
					}else{
						Set<Entry<String, ConcurrentMap<Address, Integer>>> entries = serviceMaps.entrySet();
						for(Entry<String, ConcurrentMap<Address, Integer>> entry : entries){
							String serviceName = entry.getKey();
							ConcurrentMap<Address, Integer> details = entry.getValue();
							Set<Entry<Address, Integer>> detailsSet = details.entrySet();
							for(Entry<Address, Integer> eachDetail : detailsSet){
								logger.info("service {} ,provider by address {} and weight is {}",serviceName,eachDetail.getKey(),eachDetail.getValue());
							}
									
						}
						
					}
					
					
				}
				
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}
		
	}

}
