package org.bazinga.client.provider;


public interface RegistryCenter {
	
	 RegistryCenter provider(Object serviceProvider);
	 
	 RegistryCenter provider(ProviderProxyHandler proxyHandler,Object serviceProvider);
	 

}
