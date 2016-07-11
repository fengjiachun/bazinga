package org.bazinga.client.loadbalance;

import org.bazinga.common.group.BChannelGroup;
import org.bazinga.common.group.ServiceBChannelGroup.CopyOnWriteGroupList;


public interface LoadBalance {
	
	BChannelGroup loadBalance(CopyOnWriteGroupList group);

}
