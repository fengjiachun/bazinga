package org.bazinga.example.benchmark;

import java.util.List;

import org.bazinga.client.provider.DefaultProvider;
import org.bazinga.client.provider.ServiceRegistryCenter;
import org.bazinga.client.provider.model.ServiceWrapper;
import org.bazinga.common.message.RegistryInfo.Address;
import org.bazinga.example.service.DemoServiceImpl;

public class BenchmarkServer {

	private final static int WRITE_BUFFER_HIGH_WATER_MARK = 256 * 1024;

	private final static int WRITE_BUFFER_LOW_WATER_MARK = 128 * 1024;

	public static void main(String[] args) throws Exception {
		int port = 8080;
		if (args != null && args.length > 0) {
			try {
				port = Integer.valueOf(args[0]);
			} catch (NumberFormatException e) {
				// 采用默认值
			}
		}

		List<ServiceWrapper> serviceWrappers = new ServiceRegistryCenter().provider(new DemoServiceImpl()).create();
		DefaultProvider defaultProvider = new DefaultProvider(new Address("127.0.0.1", 8899), serviceWrappers,WRITE_BUFFER_HIGH_WATER_MARK,WRITE_BUFFER_LOW_WATER_MARK);
		defaultProvider.connectToRegistryServer(port, "127.0.0.1");
		defaultProvider.start();
	}

}
