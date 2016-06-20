package org.bazinga.client.provider.model;

import java.util.List;
import java.util.Map;

public class ServiceWrapper {
	
	private Object serviceProvider;
	
	private String appName;
	
	private String responsiblityName;
	
	private transient Map<String, List<Class<?>[]>> methodsParameterTypes;
	
	private volatile int weight = 5;
	
	public ServiceWrapper(Object serviceProvider,Map<String, List<Class<?>[]>> methodsParameterTypes,String appName,String responsiblityName, int weight) {
				this.methodsParameterTypes =  methodsParameterTypes;
				this.serviceProvider = serviceProvider;
				this.appName = appName;
				this.responsiblityName = responsiblityName;
				this.weight = weight;
     }

	public Object getServiceProvider() {
		return serviceProvider;
	}

	public void setServiceProvider(Object serviceProvider) {
		this.serviceProvider = serviceProvider;
	}

	public Map<String, List<Class<?>[]>> getMethodsParameterTypes() {
		return methodsParameterTypes;
	}

	public void setMethodsParameterTypes(Map<String, List<Class<?>[]>> methodsParameterTypes) {
		this.methodsParameterTypes = methodsParameterTypes;
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
	

}
