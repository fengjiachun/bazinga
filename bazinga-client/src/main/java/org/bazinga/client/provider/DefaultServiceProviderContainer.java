package org.bazinga.client.provider;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bazinga.client.provider.model.ServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultServiceProviderContainer implements ServiceProviderContainer {
	
	protected static final Logger logger = LoggerFactory.getLogger(DefaultServiceProviderContainer.class);
	
	private final ConcurrentMap<String, ServiceWrapper> serviceProviders = new ConcurrentHashMap<String, ServiceWrapper>();

	public void registerService(String uniqueKey, ServiceWrapper serviceWrapper) {
		
		serviceProviders.put(uniqueKey, serviceWrapper);
		
		
	}

	public ServiceWrapper lookupService(String uniqueKey) {
		return serviceProviders.get(uniqueKey);
	}

}
