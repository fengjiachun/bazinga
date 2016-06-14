package org.bazinga.common.message;

import java.io.Serializable;
import java.util.Arrays;

public class RequestMessageWrapper implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6268712308008868137L;
	
	private String appName;
	
	private String serviceName;
	
	private Object[] args;

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}

	@Override
	public String toString() {
		return "RequestMessageWrapper [appName=" + appName + ", serviceName="
				+ serviceName + ", args=" + Arrays.toString(args) + "]";
	}

}
