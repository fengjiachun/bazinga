package org.bazinga.common.message;

import io.netty.channel.Channel;


public class WeightChannel {
	
	private int weight;
	
	private Channel channel;
	
	public WeightChannel() {
	
	}

	public WeightChannel(int weight, Channel channel) {
		this.weight = weight;
		this.channel = channel;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	@Override
	public String toString() {
		return "WeightChannel [weight=" + weight + ", channel=" + channel + "]";
	}

}
