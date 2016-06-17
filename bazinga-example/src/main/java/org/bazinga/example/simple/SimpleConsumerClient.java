package org.bazinga.example.simple;

import java.util.ArrayList;
import java.util.List;

import org.bazinga.client.common.utils.CommonClient;
import org.bazinga.common.message.SubScribeInfo;

public class SimpleConsumerClient {
	
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
		servicesNames.add("HelloWorldService");
		info.setServiceNames(servicesNames);
		CommonClient commonClient = new CommonClient(info);
		commonClient.connectToRegistryServer(port, "127.0.0.1");
		try {
			Object response = commonClient.call("HelloWorldService",new Object());
			if(null != response){
				if(response instanceof String){
					System.out.println("================"+(String)response);
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
	}

}
