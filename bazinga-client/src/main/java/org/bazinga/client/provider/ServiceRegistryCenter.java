package org.bazinga.client.provider;

import static net.bytebuddy.dynamic.loading.ClassLoadingStrategy.Default.INJECTION;
import static net.bytebuddy.implementation.MethodDelegation.to;
import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;
import static net.bytebuddy.matcher.ElementMatchers.not;
import net.bytebuddy.ByteBuddy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class ServiceRegistryCenter implements RegistryCenter {
	
	protected static final Logger logger = LoggerFactory.getLogger(ServiceRegistryCenter.class);
	
	//全局拦截proxy
	private volatile ProviderProxyHandler globalProviderProxyHandler;
	
	private Object serviceProvider;

	public RegistryCenter provider(Object serviceProvider) {
		if(null  == globalProviderProxyHandler){
			this.serviceProvider = serviceProvider;
		}else{
			Class<?> globalProxyCls = generateProviderProxyClass(globalProviderProxyHandler, serviceProvider.getClass());
            this.serviceProvider = copyProviderProperties(serviceProvider, newInstance(globalProxyCls));
		}
		return this;
	}

	
	private <T> Class<? extends T> generateProviderProxyClass(ProviderProxyHandler proxyHandler, Class<T> providerCls) {
		
		try {
			return new ByteBuddy()
			.subclass(providerCls)
			.method(isDeclaredBy(providerCls))
			.intercept(to(proxyHandler, "handler").filter(not(isDeclaredBy(Object.class))))
			.make()
			.load(providerCls.getClassLoader(), INJECTION)
            .getLoaded();
		} catch (Exception e) {
			logger.error("Generate proxy [{}, handler: {}] fail: {}.", providerCls, proxyHandler,e.getMessage());

            return providerCls;
		}
		
		return null;
	}


	public RegistryCenter provider(ProviderProxyHandler proxyHandler, Object serviceProvider) {
		// TODO Auto-generated method stub
		return null;
	}


	public ProviderProxyHandler getGlobalProviderProxyHandler() {
		return globalProviderProxyHandler;
	}

	public void setGlobalProviderProxyHandler(ProviderProxyHandler globalProviderProxyHandler) {
		this.globalProviderProxyHandler = globalProviderProxyHandler;
	}


}
