package org.bazinga.client.loadbalance;

import io.netty.util.internal.ConcurrentSet;

import org.bazinga.common.message.WeightChannel;


public interface LoadBalance {
	
	WeightChannel loadBalance(ConcurrentSet<WeightChannel> weightChannels);

}
