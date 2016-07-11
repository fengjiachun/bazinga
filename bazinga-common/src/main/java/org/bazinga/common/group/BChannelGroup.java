package org.bazinga.common.group;

import io.netty.channel.Channel;

/**
 * 
 * @author BazingaLyn
 * channelGroup 表示同一类的channel可以放在一个group中
 * @time 2016年7月11日
 */
public interface BChannelGroup {
	
	Channel next();
	
	boolean add(Channel channel);
	
	boolean remove(Channel channel);
	
	void setWeight(int weight);
	
	int getWeight();
	
	int size();

}
