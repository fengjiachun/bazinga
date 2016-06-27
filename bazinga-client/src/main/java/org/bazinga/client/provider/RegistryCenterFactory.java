package org.bazinga.client.provider;

import java.util.List;

import org.bazinga.client.provider.model.ServiceWrapper;



public interface RegistryCenterFactory {
	
	RegistryCenterFactory provider(Object serviceProvider);
	 
	RegistryCenterFactory provider(ProviderProxyHandler proxyHandler,Object serviceProvider);
	
	List<ServiceWrapper> create();

}
