package org.bazinga.example.simple;

import java.util.ArrayList;
import java.util.List;

import org.bazinga.client.comsumer.DefaultConsumer;
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
		new DefaultConsumer(info).connectToRegistryServer(port, "127.0.0.1");
	}

}
