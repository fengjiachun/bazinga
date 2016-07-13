package org.bazinga.web.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.bazinga.common.message.RegistryInfo.Address;
import org.bazinga.common.message.RegistryInfo.RpcService;
import org.bazinga.monitor.registryInfo.RegistryContext;
import org.bazinga.spring.support.CommonRpcMonitor;
import org.bazinga.web.vo.RpcServiceVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("bazinga")
public class CoreController {

	@Autowired
	private CommonRpcMonitor commonRpcMonitor;
	
	private RegistryContext registryContext;

	@RequestMapping("index")
	public String index() {
		return "index";
	}

	@ResponseBody
	@RequestMapping("search")
	public Map<String, Object> search(String serviceName, Integer limit, Integer offset) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		List<RpcServiceVo> rpcServiceVos = new ArrayList<RpcServiceVo>();
		
		registryContext = commonRpcMonitor.getBazingaMonitor().getRegistryContext();
		ConcurrentMap<String, ConcurrentMap<Address, RpcService>> serviceInfos = registryContext.getServiceInfo();

		Set<String> serviceNames = serviceInfos.keySet();
		if (null != serviceNames && serviceNames.size() > 0) {

			int count = 0;
			for (String eachService : serviceNames) {

				RpcServiceVo rpcServiceVo = new RpcServiceVo();

				if (serviceName != null && !StringUtils.isEmpty(serviceName)) {
					if (eachService.indexOf(serviceName) > -1) {
						rpcServiceVo.setId(count++);
						rpcServiceVo.setName(eachService);
						rpcServiceVos.add(rpcServiceVo);
					}
				}
			}
		}

		resultMap.put("total", rpcServiceVos.size());
		resultMap.put("rows", page(rpcServiceVos, limit, offset));
		return resultMap;
	}

	/**
	 * 
	 * @param rpcServiceVos
	 * @param limit
	 * @param offset
	 * @return
	 */
	private List<RpcServiceVo> page(List<RpcServiceVo> rpcServiceVos, Integer limit, Integer offset) {

		if (null != rpcServiceVos && !rpcServiceVos.isEmpty()) {
			return rpcServiceVos.subList(offset, limit+offset > rpcServiceVos.size() ? (rpcServiceVos.size()):limit+offset);
		}
		return rpcServiceVos;
	}
}
