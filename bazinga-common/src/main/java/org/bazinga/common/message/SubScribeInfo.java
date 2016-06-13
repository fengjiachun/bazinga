package org.bazinga.common.message;

import java.util.List;

public class SubScribeInfo {
	
	private List<String> serviceNames;

	public List<String> getServiceNames() {
		return serviceNames;
	}

	public void setServiceNames(List<String> serviceNames) {
		this.serviceNames = serviceNames;
	}

	@Override
	public String toString() {
		return "SubScribeInfo [serviceNames=" + serviceNames + "]";
	}
}
