package org.bazinga.client.provider;

import static net.bytebuddy.dynamic.loading.ClassLoadingStrategy.Default.INJECTION;
import static net.bytebuddy.implementation.MethodDelegation.to;
import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;
import static net.bytebuddy.matcher.ElementMatchers.not;
import static org.bazinga.common.utils.Reflects.getValue;
import static org.bazinga.common.utils.Reflects.newInstance;
import static org.bazinga.common.utils.Reflects.setValue;
import io.netty.util.internal.StringUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.bytebuddy.ByteBuddy;

import org.bazinga.client.annotation.RpcService;
import org.bazinga.client.provider.model.ServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class ServiceRegistryCenter implements RegistryCenterFactory {
	
	protected static final Logger logger = LoggerFactory.getLogger(ServiceRegistryCenter.class);
	
	//全局拦截proxy
	private volatile ProviderProxyHandler globalProviderProxyHandler;
	
	private Object serviceProvider;

	public RegistryCenterFactory provider(Object serviceProvider) {
		if(null  == globalProviderProxyHandler){
			this.serviceProvider = serviceProvider;
		}else{
			Class<?> globalProxyCls = generateProviderProxyClass(globalProviderProxyHandler, serviceProvider.getClass());
            this.serviceProvider = copyProviderProperties(serviceProvider, newInstance(globalProxyCls));
		}
		return this;
	}
	
	public RegistryCenterFactory provider(ProviderProxyHandler proxyHandler, Object serviceProvider) {
		Class<?> proxyCls = generateProviderProxyClass(proxyHandler, serviceProvider.getClass());
        if (globalProviderProxyHandler == null) {
            this.serviceProvider = copyProviderProperties(serviceProvider, newInstance(proxyCls));
        } else {
            Class<?> globalProxyCls = generateProviderProxyClass(globalProviderProxyHandler, proxyCls);
            this.serviceProvider = copyProviderProperties(serviceProvider, newInstance(globalProxyCls));
        }
        return this;
	}
	
	
	public List<ServiceWrapper> create() {
		
		List<ServiceWrapper> serviceWrappers = new ArrayList<ServiceWrapper>();
		
		RpcService rpcService = null;
		
		for (Class<?> cls = serviceProvider.getClass(); cls != Object.class; cls = cls.getSuperclass()) {
			Method[] methods = cls.getMethods();
			if(null != methods && methods.length > 0){
				
				for(Method method :methods){
					rpcService = method.getAnnotation(RpcService.class);
					if(null != rpcService){
						
						String serviceName = StringUtil.isNullOrEmpty(rpcService.serviceName())?method.getName():rpcService.serviceName();
						String appName = rpcService.appName();
						String responsiblityName = rpcService.responsibilityName();
						Integer weight = rpcService.weight();
						
						String methodName = method.getName();
						Class<?>[] classes = method.getParameterTypes();
						List<Class<?>[]> paramters = new ArrayList<Class<?>[]>();
						paramters.add(classes);
						
						ServiceWrapper serviceWrapper = new ServiceWrapper(serviceProvider,serviceName,methodName,paramters,appName,responsiblityName,weight);
						
						serviceWrappers.add(serviceWrapper);
					}
				}
			}
		}
		return serviceWrappers;
	}

	
	private <F, T> T copyProviderProperties(F provider, T proxy) {
		List<String> providerFieldNames = new ArrayList<String>();
		
		for (Class<?> cls = provider.getClass(); cls != null; cls = cls.getSuperclass()) {
            try {
                for (Field f : cls.getDeclaredFields()) {
                    providerFieldNames.add(f.getName());
                }
            } catch (Throwable ignored) {}
        }

        for (String name : providerFieldNames) {
            try {
                setValue(proxy, name, getValue(provider, name));
            } catch (Throwable ignored) {}
        }
        return proxy;
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
	}
	

	public ProviderProxyHandler getGlobalProviderProxyHandler() {
		return globalProviderProxyHandler;
	}

	public void setGlobalProviderProxyHandler(ProviderProxyHandler globalProviderProxyHandler) {
		this.globalProviderProxyHandler = globalProviderProxyHandler;
	}


}
