package org.bazinga.monitor.registryInfo;

import io.netty.util.internal.ConcurrentSet;

import java.util.ArrayList;
import java.util.List;
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
	
	//TODO
	/******一个提供服务的地址，提供的所有服务******/
	private ConcurrentMap<Address,ConcurrentSet<RpcService>> globalInfo = new ConcurrentHashMap<RegistryInfo.Address, ConcurrentSet<RpcService>>();
	
	/************一个服务，他是由哪些地址提供的,每个地址的权重多大************/
	private ConcurrentMap<String,ConcurrentMap<Address,Integer>> serviceInfo = new ConcurrentHashMap<String, ConcurrentMap<Address,Integer>>();
	
	//TODO
	/*************一个服务被哪些消费者消费****************/
	private ConcurrentMap<String,ArrayList<Address>> comsumerInfo = new ConcurrentHashMap<String, ArrayList<Address>>();
	
	
    public ConcurrentMap<Address,Integer> getProviderInfoByServiceName(String serviceName){
    	
    	return serviceInfo.get(serviceName);
    	
    }
    
    public void removeRegistryInfo(RegistryInfo registryInfo) {
		
    	/************1 STEP************/
    	Address address = registryInfo.getAddress();
    	
    	globalInfo.remove(address);
    	
    	/************2 STEP************/
    	List<RpcService> rpcServices = registryInfo.getRpcServices();
    	
    	if(null != rpcServices && rpcServices.size() > 0){
    		
    		for(RpcService rpcService:rpcServices){
    			String serviceName = rpcService.getServiceName();
    			
    			ConcurrentMap<Address, Integer> map = serviceInfo.get(serviceName);
    			
    			if(null != map){
    				map.remove(address);
    				
    			}
    			
    		}
    	}
    	
	}
    
	public void registryCurrentInfo(RegistryInfo registryInfo) {
		
		/************1 STEP************/
		Address address = registryInfo.getAddress();
		
		ConcurrentSet<RpcService> rpcServices = globalInfo.get(address);
		
		if(null == rpcServices) {
			rpcServices = new ConcurrentSet<RegistryInfo.RpcService>();
		}
		rpcServices.addAll(registryInfo.getRpcServices());
		
		
		//TODO NEED ?
		globalInfo.put(address, rpcServices);
		
		/************2 STEP************/
		List<RpcService> rpcServicesList = registryInfo.getRpcServices();
		
		if(null != rpcServicesList && !rpcServicesList.isEmpty()){
			
			for(RpcService rpcService : rpcServicesList){
				
				String serviceName = rpcService.getServiceName();
				
				ConcurrentMap<Address,Integer> map = serviceInfo.get(serviceName);
				
				if(null == map){
					map = new ConcurrentHashMap<RegistryInfo.Address, Integer>();
				}
				map.put(address, rpcService.getWeight());
				
				serviceInfo.put(serviceName, map);
			}
		}
	}

	public ConcurrentMap<Address, ConcurrentSet<RpcService>> getGlobalInfo() {
		return globalInfo;
	}

	public void setGlobalInfo(ConcurrentMap<Address, ConcurrentSet<RpcService>> globalInfo) {
		this.globalInfo = globalInfo;
	}

	public ConcurrentMap<String, ConcurrentMap<Address, Integer>> getServiceInfo() {
		return serviceInfo;
	}

	public void setServiceInfo(ConcurrentMap<String, ConcurrentMap<Address, Integer>> serviceInfo) {
		this.serviceInfo = serviceInfo;
	}

	public ConcurrentMap<String, ArrayList<Address>> getComsumerInfo() {
		return comsumerInfo;
	}

	public void setComsumerInfo(ConcurrentMap<String, ArrayList<Address>> comsumerInfo) {
		this.comsumerInfo = comsumerInfo;
	}


}
