package org.bazinga.common.message;

import org.bazinga.common.message.RegistryInfo.Address;

public class ProviderInfo {
	
	private Address address;
	
	private Integer weight;
	
	public ProviderInfo() {
		// TODO Auto-generated constructor stub
	}
	
	
	public ProviderInfo(Address address, Integer weight) {
		this.address = address;
		this.weight = weight;
	}



	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public Integer getWeight() {
		return weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}


	@Override
	public String toString() {
		return "ProviderInfo [address=" + address + ", weight=" + weight + "]";
	}

}
