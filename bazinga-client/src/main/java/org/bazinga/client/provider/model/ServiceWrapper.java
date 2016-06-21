package org.bazinga.client.provider.model;

import java.util.List;

public class ServiceWrapper {
	
	private Object serviceProvider;
	
	private String appName;
	
	private String responsiblityName;
	
	private String serviceName;
	
	private String methodName;
	
	private List<Class<?>[]> paramters;
	
	private volatile int weight = 5;
	
	public ServiceWrapper(Object serviceProvider,String serviceName,String methodName,List<Class<?>[]> paramters,String appName,String responsiblityName, int weight) {
				this.serviceName =  serviceName;
				this.paramters = paramters;
				this.serviceProvider = serviceProvider;
				this.appName = appName;
				this.responsiblityName = responsiblityName;
				this.weight = weight;
				this.methodName = methodName;
     }

	public Object getServiceProvider() {
		return serviceProvider;
	}

	public void setServiceProvider(Object serviceProvider) {
		this.serviceProvider = serviceProvider;
	}


	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public List<Class<?>[]> getParamters() {
		return paramters;
	}

	public void setParamters(List<Class<?>[]> paramters) {
		this.paramters = paramters;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getResponsiblityName() {
		return responsiblityName;
	}

	public void setResponsiblityName(String responsiblityName) {
		this.responsiblityName = responsiblityName;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	
	

}
