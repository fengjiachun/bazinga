package org.bazinga.spring.support;

import java.util.List;

import org.bazinga.client.common.utils.CommonClient;
import org.bazinga.client.common.utils.DefaultCommonClient;
import org.bazinga.common.message.SubScribeInfo;
import org.springframework.beans.factory.InitializingBean;

/**
 * 消费者端对spring的支持
 * @author BazingaLyn
 *
 * @time
 */
public class CommonRpcClient implements DefaultCommonClient, InitializingBean {
	
	/*****真实的对象****/
	private CommonClient commonClient;
	
	/*******订阅的sericeName******/
	private List<String> serviceNames;
	
	/****monitor的端口号***/
	private int port;
	
	/*****monitor的host******/
	private String host;
	
	private SubScribeInfo scribeInfo;


	public void afterPropertiesSet() throws Exception {
		scribeInfo = new SubScribeInfo();
		scribeInfo.setServiceNames(serviceNames);
		commonClient = new CommonClient(scribeInfo);
		commonClient.connectToRegistryServer(port, host);
	}
	
	public Object call(String serviceName, Object... args) throws Throwable {
		return commonClient.call(serviceName, args);
	}


	@Override
	public Object call(String serviceName, long timeOut, Object... args) throws Throwable {
		return commonClient.call(serviceName,timeOut, args);
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

}
