package org.bazinga.client.loadbalance;

import io.netty.channel.Channel;
import io.netty.util.internal.ConcurrentSet;

import org.bazinga.common.message.WeightChannel;

public interface LoadBalance {
	
	Channel loadBalance(ConcurrentSet<WeightChannel> channels);

}
