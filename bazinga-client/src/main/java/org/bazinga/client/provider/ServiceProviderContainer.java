package org.bazinga.client.provider;

import org.bazinga.client.provider.model.ServiceWrapper;

public interface ServiceProviderContainer {
	
	void registerService(String uniqueKey, ServiceWrapper serviceWrapper);

    ServiceWrapper lookupService(String uniqueKey);

}
