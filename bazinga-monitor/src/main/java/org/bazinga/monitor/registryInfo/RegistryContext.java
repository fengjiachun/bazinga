package org.bazinga.monitor.registryInfo;

import io.netty.util.internal.ConcurrentSet;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bazinga.common.message.RegistryInfo;
import org.bazinga.common.message.RegistryInfo.Address;
import org.bazinga.common.message.RegistryInfo.RpcService;

/**
 * 
 *
 */
public class RegistryContext {
	
	private ConcurrentMap<Address,ConcurrentSet<RpcService>> globalInfo = new ConcurrentHashMap<RegistryInfo.Address, ConcurrentSet<RpcService>>();
	
	private ConcurrentMap<RpcService,ConcurrentMap<Address,Integer>> serviceInfo = new ConcurrentHashMap<RegistryInfo.RpcService, ConcurrentMap<Address,Integer>>();
	
	
	
	
	

}
