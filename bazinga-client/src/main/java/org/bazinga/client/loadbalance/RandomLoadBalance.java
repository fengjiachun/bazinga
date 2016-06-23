package org.bazinga.client.loadbalance;

import io.netty.util.internal.ConcurrentSet;

import java.util.concurrent.ThreadLocalRandom;

import org.bazinga.common.message.WeightChannel;



/**
 * 随机加权重的负载均衡算法
 */
public class RandomLoadBalance implements LoadBalance {

	public WeightChannel loadBalance(ConcurrentSet<WeightChannel> weightChannels) {
		
		int count = weightChannels.size();
		if (count == 0) {
            throw new IllegalArgumentException("empty elements for select");
        }
		Object[] wcObjects = weightChannels.toArray();
		if(count == 1){
			return (WeightChannel)(wcObjects[0]);
		}
		int totalWeight = 0;
        int[] weightSnapshots = new int[count];
        for (int i = 0; i < count; i++) {
            totalWeight += (weightSnapshots[i] = getWeight((WeightChannel) wcObjects[i]));
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
                    return (WeightChannel) wcObjects[i];
                }
            }
        }

        return (WeightChannel) wcObjects[random.nextInt(count)];
		
	}

	private int getWeight(WeightChannel weightChannel) {
		return weightChannel.getWeight();
	}


	 


}
