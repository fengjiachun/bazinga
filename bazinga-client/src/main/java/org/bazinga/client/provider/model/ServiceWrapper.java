package org.bazinga.client.provider.model;

import java.util.List;
import static org.bazinga.common.utils.Constants.DEFAULT_CONNECTION_COUNT;
import static org.bazinga.common.utils.Constants.DEFAULT_WEIGHT;

/**
 * 提供者每个服务抽象出来的服务编织类
 * 根据这个类可以用来对原生的类的调用
 * @author BazingaLyn
 * @copyright fjc
 * @time fixtime 2016年7月13日 add #connCount
 */
public class ServiceWrapper {
	
	/****原生类****/
	private Object serviceProvider;
	
	/******提供该服务的系统名*****/
	private String appName;
	
	/******该系统的负责人*******/
	private String responsiblityName;
	
	/******服务名*****/
	private String serviceName;
	
	/*******该类中的方法名*******/
	private String methodName;
	
	/******该方法的入参******/
	private List<Class<?>[]> paramters;
	
	private volatile int weight = DEFAULT_WEIGHT;
	
	private volatile int connCount = DEFAULT_CONNECTION_COUNT;
	
	public ServiceWrapper(Object serviceProvider,String serviceName,String methodName,List<Class<?>[]> paramters,String appName,String responsiblityName, int weight,int connCount) {
				this.serviceName =  serviceName;
				this.paramters = paramters;
				this.serviceProvider = serviceProvider;
				this.appName = appName;
				this.responsiblityName = responsiblityName;
				this.weight = weight;
				this.methodName = methodName;
				this.connCount = connCount;
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

	public int getConnCount() {
		return connCount;
	}

	public void setConnCount(int connCount) {
		this.connCount = connCount;
	}

	@Override
	public String toString() {
		return "ServiceWrapper [serviceProvider=" + serviceProvider + ", appName=" + appName + ", responsiblityName=" + responsiblityName + ", serviceName="
				+ serviceName + ", methodName=" + methodName + ", paramters=" + paramters + ", weight=" + weight + ", connCount=" + connCount + "]";
	}

}
