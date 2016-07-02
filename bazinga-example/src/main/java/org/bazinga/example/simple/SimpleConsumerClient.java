package org.bazinga.example.simple;

import java.util.ArrayList;
import java.util.List;

import org.bazinga.client.common.utils.CommonClient;
import org.bazinga.common.logger.InternalLogger;
import org.bazinga.common.logger.InternalLoggerFactory;
import org.bazinga.common.message.SubScribeInfo;
import org.bazinga.example.benchmark.BenchmarkClient;

public class SimpleConsumerClient {
	
	private static final InternalLogger logger = InternalLoggerFactory.getInstance(BenchmarkClient.class);
	
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		int port = 8080;
		if (args != null && args.length > 0) {
			try {
				port = Integer.valueOf(args[0]);
			} catch (NumberFormatException e) {
				// 采用默认值
			}
		}
		SubScribeInfo info = new SubScribeInfo();
		List<String> servicesNames = new ArrayList<String>();
		servicesNames.add("BAZINGA.NM.DEMOSERVICE.SAYHELLO");
		info.setServiceNames(servicesNames);
		CommonClient commonClient = new CommonClient(info);
		commonClient.connectToRegistryServer(port, "127.0.0.1");
		
		try {
			for(int i = 0;i<1000;i++){
				i++;
				Object response = commonClient.call("BAZINGA.NM.DEMOSERVICE.SAYHELLO", "LIYUAN");
				if (null != response) {
					if (response instanceof String) {
						logger.info("================" + (String) response);
					}
				}
			}
			
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
	}
	

}
