package org.bazinga.common.message;

import java.util.List;

public class ProviderInfos {
	
	private String serviceName;
	
	private List<ProviderInfo> providers;
	
	public ProviderInfos() {
		
	}

	public ProviderInfos(String serviceName, List<ProviderInfo> providers) {
		this.serviceName = serviceName;
		this.providers = providers;
	}
	
	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public List<ProviderInfo> getProviders() {
		return providers;
	}

	public void setProviders(List<ProviderInfo> providers) {
		this.providers = providers;
	}

	@Override
	public String toString() {
		return "ProviderInfos [serviceName=" + serviceName + ", providers="
				+ providers + "]";
	}

}
