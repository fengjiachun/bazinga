package org.bazinga.common.message;

import java.util.List;

/**
 * 订阅的服务名
 * 服务名则是简单的String类型
 * @author BazingaLyn
 * @time
 */
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
