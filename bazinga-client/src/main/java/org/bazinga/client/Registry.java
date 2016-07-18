package org.bazinga.client;

/**
 * 注册抽象
 * @author BazingaLyn
 * @copyright fjc
 * @time
 */
public interface Registry {
	
	void connectToRegistryServer(int port, String host) throws Exception;

}
