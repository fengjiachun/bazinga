package org.bazinga.common.message;

import org.bazinga.common.message.RegistryInfo.Address;

public class ProviderInfo {
	
	private Address address;
	
	private int weight;
	
	private int connCount;
	
	
	public ProviderInfo() {
	}
	
	public ProviderInfo(Address address, int weight,int connCount) {
		this.address = address;
		this.weight = weight;
		this.connCount = connCount;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
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
		return "ProviderInfo [address=" + address + ", weight=" + weight + ", connCount=" + connCount + "]";
	}

}
