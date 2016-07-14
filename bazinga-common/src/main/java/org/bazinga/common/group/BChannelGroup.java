package org.bazinga.common.group;

import io.netty.channel.Channel;

/**
 * 
 * @author BazingaLyn
 * channelGroup 无状态的Group,与服务无关，只是表示某台实例的channel的集合
 * @copyright fjc
 * @time 2016年7月11日
 */
public interface BChannelGroup {
	
	Channel next();
	
	boolean add(Channel channel);
	
	boolean remove(Channel channel);
	
	void setWeight(int weight);
	
	int getWeight();
	
	int size();
	
	boolean isAvailable();

}
