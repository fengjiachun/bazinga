package org.bazinga.common.message;

import java.util.List;

public class RegistryInfo {
	
	private Address address;
	
	private String appName;
	
	private String responsibilityUser;
	
	private List<RpcService> rpcServices;
	
	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
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

	public List<RpcService> getRpcServices() {
		return rpcServices;
	}

	public void setRpcServices(List<RpcService> rpcServices) {
		this.rpcServices = rpcServices;
	}
	

	@Override
	public String toString() {
		return "RegistryInfo [address=" + address + ", appName=" + appName
				+ ", responsibilityUser=" + responsibilityUser
				+ ", rpcServices=" + rpcServices + "]";
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
	
	public static class RpcService {
		
		private String serviceName;
		
		private int weight;
		
		public RpcService() {
			// TODO Auto-generated constructor stub
		}
		
		public RpcService(String serviceName, int weight) {
			this.serviceName = serviceName;
			this.weight = weight;
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

		@Override
		public String toString() {
			return "RpcService [serviceName=" + serviceName + ", weight="
					+ weight + "]";
		}
		
	}

}
