package org.bazinga.client.loadbalance;

import java.util.concurrent.ThreadLocalRandom;

import org.bazinga.common.group.BChannelGroup;
import org.bazinga.common.group.ServiceBChannelGroup.CopyOnWriteGroupList;



/**
 * 随机加权重的负载均衡算法
 */
public class RandomLoadBalance implements LoadBalance {

	public BChannelGroup loadBalance(CopyOnWriteGroupList group) {
		
		int count = group.size();
		if (count == 0) {
            throw new IllegalArgumentException("empty elements for select");
        }
		Object[] wcObjects = group.toArray();
		if(count == 1){
			return (BChannelGroup)(wcObjects[0]);
		}
		int totalWeight = 0;
        int[] weightSnapshots = new int[count];
        for (int i = 0; i < count; i++) {
            totalWeight += (weightSnapshots[i] = getWeight((BChannelGroup) wcObjects[i]));
        }


        boolean allSameWeight = true;
        for (int i = 1; i < count; i++) {
            if (weightSnapshots[0] != weightSnapshots[i]) {
                allSameWeight = false;
                break;
            }
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        // 如果权重不相同且总权重大于0, 则按总权重数随机
        if (!allSameWeight && totalWeight > 0) {
            int offset = random.nextInt(totalWeight);
            // 确定随机值落在哪个片
            for (int i = 0; i < count; i++) {
                offset -= weightSnapshots[i];
                if (offset < 0) {
                    return (BChannelGroup) wcObjects[i];
                }
            }
        }

        return (BChannelGroup) wcObjects[random.nextInt(count)];
		
	}

	private int getWeight(BChannelGroup channelGroup) {
		return channelGroup.getWeight();
	}


	 


}
