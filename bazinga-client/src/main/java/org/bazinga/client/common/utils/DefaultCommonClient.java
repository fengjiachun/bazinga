package org.bazinga.client.common.utils;

public interface DefaultCommonClient {
	
	Object call(String serviceName,Object... args) throws Throwable;

}
