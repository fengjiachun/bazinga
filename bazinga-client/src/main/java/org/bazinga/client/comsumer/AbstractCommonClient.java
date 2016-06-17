package org.bazinga.client.comsumer;

import io.netty.channel.Channel;
import io.netty.util.internal.ConcurrentSet;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bazinga.common.message.WeightChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCommonClient {
	
	protected static final Logger logger = LoggerFactory.getLogger(AbstractCommonClient.class);

	private volatile ConcurrentMap<String, ConcurrentSet<WeightChannel>> serviceChannel = new ConcurrentHashMap<String, ConcurrentSet<WeightChannel>>();

	public void addInfo(String serviceName, Channel channel, int weight) {
		
		logger.info("{} 服务 建立好的连接 {}",serviceName,channel);
		
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
