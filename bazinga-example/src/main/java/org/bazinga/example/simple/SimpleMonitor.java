package org.bazinga.example.simple;

import io.netty.util.internal.ConcurrentSet;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.bazinga.common.logger.InternalLogger;
import org.bazinga.common.logger.InternalLoggerFactory;
import org.bazinga.common.message.RegistryInfo.Address;
import org.bazinga.common.message.RegistryInfo.RpcService;
import org.bazinga.monitor.BazingaMonitor;
import org.bazinga.monitor.registryInfo.RegistryContext;

public class SimpleMonitor {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(SimpleMonitor.class);

	private static BazingaMonitor bazingaMonitor = null;
	
	private static final SimpleDateFormat simpleDateFormat =  new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

	public static void main(String[] args) throws InterruptedException {
		int port;
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		} else {
			port = 18899;
		}
		bazingaMonitor = new BazingaMonitor(port);
		Thread thread = new Thread(new MonitorScanner(),"monitor.console.scanner");
		thread.start();
		bazingaMonitor.start();
	}
	
	public static class MonitorScanner implements Runnable {

		public void run() {
			logger.info("/*******监控者开始循环注册信息**********/");
			
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
						logger.info("当前时间 {} 没有地址提供任何服务",simpleDateFormat.format(new Date()));
					}else{
						Set<Entry<Address, ConcurrentSet<RpcService>>> entries = concurrentMap.entrySet();
						
						for(Entry<Address, ConcurrentSet<RpcService>> entry : entries){
							Address address = entry.getKey();
							ConcurrentSet<RpcService> servicesList = entry.getValue();
							for(RpcService rpcService :servicesList){
								logger.info("当前时间 {} 地址是 {} 提供{}服务",simpleDateFormat.format(new Date()),address,rpcService.getServiceName());
							}
							
						}
					}
					
					
					ConcurrentMap<String, ConcurrentMap<Address, RpcService>> serviceMaps = registryContext.getServiceInfo();
					
					if(serviceMaps.keySet().isEmpty()){
						logger.info("当前时间 {} 没有任何服务",simpleDateFormat.format(new Date()));
					}else{
						Set<Entry<String, ConcurrentMap<Address, RpcService>>> entries = serviceMaps.entrySet();
						for(Entry<String, ConcurrentMap<Address, RpcService>> entry : entries){
							String serviceName = entry.getKey();
							ConcurrentMap<Address, RpcService> details = entry.getValue();
							Set<Entry<Address, RpcService>> detailsSet = details.entrySet();
							for(Entry<Address, RpcService> eachDetail : detailsSet){
								logger.info("服务 {} ,是由{}该地址提供的并且它的负重是{},连接数是{}",serviceName,eachDetail.getKey(),eachDetail.getValue().getWeight(),eachDetail.getValue().getConnCount());
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
