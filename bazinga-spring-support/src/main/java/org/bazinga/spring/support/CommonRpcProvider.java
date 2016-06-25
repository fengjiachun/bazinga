package org.bazinga.spring.support;

import java.util.ArrayList;
import java.util.List;

import org.bazinga.client.provider.DefaultProvider;
import org.bazinga.client.provider.ServiceRegistryCenter;
import org.bazinga.client.provider.model.ServiceWrapper;
import org.bazinga.common.message.RegistryInfo.Address;
import org.springframework.beans.factory.InitializingBean;

/**
 * 
 * @author BazingaLyn 说明：提供者对spring的支持
 * @time 2016年6月24日
 */
public class CommonRpcProvider implements InitializingBean {

	private List<Object> objects;

	private int registryPort;

	private String registryHost;

	private int providerPort;

	private String providerHost;
	
	private DefaultProvider defaultProvider;

	public void afterPropertiesSet() throws Exception {
		
		List<ServiceWrapper> allServiceWrappers = new ArrayList<ServiceWrapper>();
		for (Object o : objects) {
			List<ServiceWrapper> serviceWrappers = new ServiceRegistryCenter().provider(o).create();
			allServiceWrappers.addAll(serviceWrappers);
		}
		if (allServiceWrappers.size() > 0) {
			defaultProvider = new DefaultProvider(new Address(providerHost, providerPort),allServiceWrappers);
			defaultProvider.connectToRegistryServer(registryPort, registryHost);
		    defaultProvider.start();
		}

	}

	public List<Object> getObjects() {
		return objects;
	}

	public void setObjects(List<Object> objects) {
		this.objects = objects;
	}

	public int getRegistryPort() {
		return registryPort;
	}

	public void setRegistryPort(int registryPort) {
		this.registryPort = registryPort;
	}

	public String getRegistryHost() {
		return registryHost;
	}

	public void setRegistryHost(String registryHost) {
		this.registryHost = registryHost;
	}

	public int getProviderPort() {
		return providerPort;
	}

	public void setProviderPort(int providerPort) {
		this.providerPort = providerPort;
	}

	public String getProviderHost() {
		return providerHost;
	}

	public void setProviderHost(String providerHost) {
		this.providerHost = providerHost;
	}

}
