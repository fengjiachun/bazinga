package org.bazinga.client.common.utils;

public interface DefaultCommonClient {
	
	Object call(String serviceName,Object... args) throws Throwable;
	
	Object call(String serviceName,long timeOut,Object... args) throws Throwable;

}
