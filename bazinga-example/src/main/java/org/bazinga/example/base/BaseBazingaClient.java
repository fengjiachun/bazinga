package org.bazinga.example.base;

import java.util.ArrayList;
import java.util.List;

import org.bazinga.client.common.utils.CommonClient;
import org.bazinga.common.logger.InternalLogger;
import org.bazinga.common.logger.InternalLoggerFactory;
import org.bazinga.common.message.SubScribeInfo;
import org.bazinga.example.benchmark.BenchmarkClient;

public class BaseBazingaClient {
	
	private static final InternalLogger logger = InternalLoggerFactory.getInstance(BenchmarkClient.class);
	
	public static void main(String[] args) throws Throwable {
		SubScribeInfo info = new SubScribeInfo();
		List<String> servicesNames = new ArrayList<String>();
		servicesNames.add("BAZINGA.NM.DEMOSERVICE.SAYHELLO");
		info.setServiceNames(servicesNames);
		CommonClient commonClient = new CommonClient(info);
		commonClient.connectToRegistryServer(20001, "127.0.0.1");
		
			for(int i = 0;i<1000;i++){
				i++;
				Object response = commonClient.call("BAZINGA.NM.DEMOSERVICE.SAYHELLO", "LIYUAN");
				if (null != response) {
					if (response instanceof String) {
						logger.info("================" + (String) response);
					}
				}
			}
			
	}

}
