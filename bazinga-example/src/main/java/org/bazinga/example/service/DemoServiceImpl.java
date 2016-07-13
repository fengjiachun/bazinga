package org.bazinga.example.service;

import org.bazinga.client.annotation.RpcService;

public class DemoServiceImpl {
	
	@RpcService(responsibilityName="bazinga",serviceName="BAZINGA.NM.DEMOSERVICE.SAYHELLO",appName="BAZ",weight=5,connCount = 4)
	public String sayHello(String hello){
		return "hello"+hello;
	}

}
