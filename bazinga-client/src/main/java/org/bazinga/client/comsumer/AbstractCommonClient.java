package org.bazinga.client.comsumer;

import io.netty.channel.Channel;
import io.netty.util.internal.ConcurrentSet;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bazinga.client.loadbalance.RandomLoadBalance;
import org.bazinga.common.logger.InternalLogger;
import org.bazinga.common.logger.InternalLoggerFactory;
import org.bazinga.common.message.WeightChannel;

/**
 * 该类主要维护了某个服务的channel
 * @author BazingaLyn
 *
 * @time
 */
public abstract class AbstractCommonClient extends RandomLoadBalance {
	
	
	private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractCommonClient.class);

	/****维护每个服务对应的channel*****/
	private volatile ConcurrentMap<String, ConcurrentSet<WeightChannel>> serviceChannel = new ConcurrentHashMap<String, ConcurrentSet<WeightChannel>>();

	/**
	 * 增加某个服务到已有的Map中
	 * @param serviceName
	 * @param channel
	 * @param weight
	 */
	public void addInfo(String serviceName, Channel channel, int weight) {
		
		logger.info("{} 服务 建立好的连接 {}",serviceName,channel);
		
		//获取该服务现有的channels
		ConcurrentSet<WeightChannel> concurrentSet = serviceChannel.get(serviceName);
		//目前没有，则新建
		if(null == concurrentSet) {
			concurrentSet = new ConcurrentSet<WeightChannel>();
		}
		//创建一个channel
		WeightChannel newWeightChannel = new WeightChannel(weight,channel);
		//防止在value集合中
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
