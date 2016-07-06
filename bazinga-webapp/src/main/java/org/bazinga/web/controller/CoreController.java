package org.bazinga.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.bazinga.common.message.RegistryInfo.Address;
import org.bazinga.monitor.registryInfo.RegistryContext;
import org.bazinga.spring.support.CommonRpcMonitor;
import org.bazinga.web.vo.RpcServiceVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("bazinga")
public class CoreController {
	
	@Autowired
	private CommonRpcMonitor commonRpcMonitor;

	@RequestMapping("index")
	public String index(){
		return "index";
	}
	
	@ResponseBody
	@RequestMapping("search")
	public List<RpcServiceVo> search(String serviceName){
		
		List<RpcServiceVo> rpcServiceVos = new ArrayList<RpcServiceVo>();
		
		RegistryContext registryContext = commonRpcMonitor.getBazingaMonitor().getRegistryContext();
		
		ConcurrentMap<String, ConcurrentMap<Address, Integer>> serviceInfos = registryContext.getServiceInfo();
		
		Set<String> serviceNames = serviceInfos.keySet();
		
		if(null != serviceNames && serviceNames.size() > 0){
			
			for(String eachService :serviceNames){
				
				RpcServiceVo rpcServiceVo = new RpcServiceVo();
				
				if(serviceName != null){
					if(eachService.indexOf(serviceName) > 0){
						rpcServiceVo.setName(eachService);
						rpcServiceVos.add(rpcServiceVo);
					}
				}
			}
		}
		return rpcServiceVos;
	}
}
