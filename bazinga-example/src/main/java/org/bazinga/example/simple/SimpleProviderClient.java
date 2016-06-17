package org.bazinga.example.simple;

import java.util.ArrayList;
import java.util.List;

import org.bazinga.client.provider.DefaultProvider;
import org.bazinga.common.message.RegistryInfo;
import org.bazinga.common.message.RegistryInfo.Address;
import org.bazinga.common.message.RegistryInfo.RpcService;

public class SimpleProviderClient {
	
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
        RegistryInfo info = new RegistryInfo();
        info.setAddress(new Address("127.0.0.1", 8899));
        info.setAppName("Bazinga");
        info.setResponsibilityUser("Lyncc");
        RpcService rpcService1 = new RpcService("HelloWorldService",5);
        RpcService rpcService2 = new RpcService("RpcService",5);
        List<RpcService> rpcServices = new ArrayList<RegistryInfo.RpcService>();
        rpcServices.add(rpcService1);
        rpcServices.add(rpcService2);
        info.setRpcServices(rpcServices);
        DefaultProvider defaultProvider = new DefaultProvider(info);
        defaultProvider.connectToRegistryServer(port, "127.0.0.1");
        defaultProvider.start();
    }

}
