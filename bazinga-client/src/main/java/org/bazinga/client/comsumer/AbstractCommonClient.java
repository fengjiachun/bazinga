package org.bazinga.client.comsumer;

import io.netty.channel.Channel;
import io.netty.util.internal.ConcurrentSet;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bazinga.common.message.WeightChannel;

public abstract class AbstractCommonClient {

	private volatile ConcurrentMap<String, ConcurrentSet<WeightChannel>> serviceChannel = new ConcurrentHashMap<String, ConcurrentSet<WeightChannel>>();

	public void addInfo(String serviceName, Channel channel, int weight) {
		
		ConcurrentSet<WeightChannel> concurrentSet = serviceChannel.get(serviceName);
		
		if(null == concurrentSet) {
			concurrentSet = new ConcurrentSet<WeightChannel>();
		}
		
		WeightChannel newWeightChannel = new WeightChannel(weight,channel);
		
		concurrentSet.add(newWeightChannel);
		
		serviceChannel.put(serviceName, concurrentSet);
	}

	public ConcurrentSet<WeightChannel> getAllMatchedChannel(String serviceName) {
		ConcurrentSet<WeightChannel> channels = serviceChannel.get(serviceName);
		if(null == channels){
			channels = new ConcurrentSet<WeightChannel>();
			serviceChannel.put(serviceName, channels);
		}
		return channels;
		
	}
	

}
