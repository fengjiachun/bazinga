package org.bazinga.client.comsumer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bazinga.client.loadbalance.RandomLoadBalance;
import org.bazinga.common.UnresolvedAddress;
import org.bazinga.common.group.BChannelGroup;
import org.bazinga.common.group.NettyChannelGroup;

/**
 * 该类主要维护了某个服务的channelGroup
 * 
 * @author BazingaLyn
 * @copyright fjc
 * @time
 */
public abstract class AbstractCommonClient extends RandomLoadBalance {

	/**** 维护每个服务对应的channelgroup *****/
	protected final ConcurrentMap<UnresolvedAddress, BChannelGroup> addressGroups = new ConcurrentHashMap<UnresolvedAddress, BChannelGroup>();

	protected BChannelGroup group(UnresolvedAddress address) {

		BChannelGroup group = addressGroups.get(address);
		if (group == null) {
			BChannelGroup newGroup = newChannelGroup(address);
			group = addressGroups.putIfAbsent(address, newGroup);
			if (group == null) {
				group = newGroup;
			}
		}
		return group;
	}

	private BChannelGroup newChannelGroup(UnresolvedAddress address) {
		return new NettyChannelGroup(address);
	}

}
