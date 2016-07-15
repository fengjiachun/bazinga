package org.bazinga.example.base;

import java.util.List;

import org.bazinga.client.provider.DefaultProvider;
import org.bazinga.client.provider.ServiceRegistryCenter;
import org.bazinga.client.provider.model.ServiceWrapper;
import org.bazinga.common.message.RegistryInfo.Address;
import org.bazinga.example.service.DemoServiceImpl;

public class BaseBazingaService {
	
	public static void main(String[] args) throws Exception {
        List<ServiceWrapper> serviceWrappers = new ServiceRegistryCenter().provider(new DemoServiceImpl()).create();
        DefaultProvider defaultProvider = new DefaultProvider(new Address("127.0.0.1", 8899),serviceWrappers);
        defaultProvider.connectToRegistryServer(20001, "127.0.0.1");
        defaultProvider.start();
	}

}
