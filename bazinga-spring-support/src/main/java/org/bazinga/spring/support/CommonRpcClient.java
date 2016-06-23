package org.bazinga.spring.support;

import java.util.List;

import org.bazinga.client.common.utils.CommonClient;
import org.bazinga.client.common.utils.DefaultCommonClient;
import org.bazinga.common.message.SubScribeInfo;
import org.springframework.beans.factory.InitializingBean;

public class CommonRpcClient implements DefaultCommonClient, InitializingBean {
	
	private CommonClient commonClient;
	
	private List<String> serviceNames;
	
	private int port;
	
	private String host;
	
	private SubScribeInfo scribeInfo = new SubScribeInfo();


	public void afterPropertiesSet() throws Exception {
		scribeInfo.setServiceNames(serviceNames);
		commonClient = new CommonClient(scribeInfo);
		commonClient.connectToRegistryServer(port, host);
	}


	public List<String> getServiceNames() {
		return serviceNames;
	}


	public void setServiceNames(List<String> serviceNames) {
		this.serviceNames = serviceNames;
	}
	

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}


	public String getHost() {
		return host;
	}


	public void setHost(String host) {
		this.host = host;
	}


	public Object call(String serviceName, Object... args) throws Throwable {
		return commonClient.call(serviceName, args);
	}

}
