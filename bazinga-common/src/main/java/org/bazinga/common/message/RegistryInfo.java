package org.bazinga.common.message;

import java.util.List;

/**
 * 向注册中心发送的注册信息
 * @author BazingaLyn
 * @copyright fjc
 * @time 2016年6月11日 modifyTime 2016年7月13日 addConnCount 
 */
public class RegistryInfo {

	//提供该服务的地址
	private Address address;

	//该地址上的所有服务列表
	private List<RpcService> rpcServices;

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public List<RpcService> getRpcServices() {
		return rpcServices;
	}

	public void setRpcServices(List<RpcService> rpcServices) {
		this.rpcServices = rpcServices;
	}

	@Override
	public String toString() {
		return "RegistryInfo [address=" + address + ", rpcServices=" + rpcServices + "]";
	}

	public static class Address {

		private String host;

		private int port;

		public Address(String host, int port) {
			this.host = host;
			this.port = port;
		}

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		@Override
		public String toString() {
			return "Address [host=" + host + ", port=" + port + "]";
		}

	}

	//服务信息
	public static class RpcService {

		//服务名
		private String serviceName;

		//当前实例服务的权重
		private int weight;

		//服务所属的引用名
		private String appName;

		//该服务的负责人
		private String responsibilityUser;

		//链接数
		private int connCount;

		public RpcService() {

		}

		public RpcService(String serviceName, int weight) {
			this.serviceName = serviceName;
			this.weight = weight;
		}

		public RpcService(String serviceName, int weight, String appName, String responsibilityUser, int connCount) {
			this.serviceName = serviceName;
			this.weight = weight;
			this.appName = appName;
			this.responsibilityUser = responsibilityUser;
			this.connCount = connCount;
		}

		public String getAppName() {
			return appName;
		}

		public void setAppName(String appName) {
			this.appName = appName;
		}

		public String getResponsibilityUser() {
			return responsibilityUser;
		}

		public void setResponsibilityUser(String responsibilityUser) {
			this.responsibilityUser = responsibilityUser;
		}

		public String getServiceName() {
			return serviceName;
		}

		public void setServiceName(String serviceName) {
			this.serviceName = serviceName;
		}

		public int getWeight() {
			return weight;
		}

		public void setWeight(int weight) {
			this.weight = weight;
		}

		public int getConnCount() {
			return connCount;
		}

		public void setConnCount(int connCount) {
			this.connCount = connCount;
		}

		@Override
		public String toString() {
			return "RpcService [serviceName=" + serviceName + ", weight=" + weight + ", appName=" + appName + ", responsibilityUser=" + responsibilityUser
					+ ", connCount=" + connCount + "]";
		}

	}

}
