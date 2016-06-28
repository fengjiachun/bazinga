package org.bazinga.example.benchmark;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import org.bazinga.client.common.utils.CommonClient;
import org.bazinga.common.message.SubScribeInfo;

/**
 * 第一次性能测试结果：
 * count=128000
 * Request count: 128000, time: 90 second, qps: 1422
 * 
 * 
 * 第二次：优化日记配置
 * count=128000 反而变得更慢 (＞﹏＜)
 *Request count: 128000, time: 103 second, qps: 1242
 * 
 */
public class BenchmarkClient {

	public static void main(String[] args) throws Exception {

		int processors = Runtime.getRuntime().availableProcessors();

		int port = 8080;
		if (args != null && args.length > 0) {
			try {
				port = Integer.valueOf(args[0]);
			} catch (NumberFormatException e) {
				// 采用默认值
			}
		}
		SubScribeInfo info = new SubScribeInfo();
		List<String> servicesNames = new ArrayList<String>();
		servicesNames.add("BAZINGA.NM.DEMOSERVICE.SAYHELLO");
		info.setServiceNames(servicesNames);
		final CommonClient commonClient = new CommonClient(info);
		commonClient.connectToRegistryServer(port, "127.0.0.1");

		try {
			Object response = commonClient.call("BAZINGA.NM.DEMOSERVICE.SAYHELLO", "LIYUAN");
			if (null != response) {
				if (response instanceof String) {
					System.out.println("================" + (String) response);
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

		final int t = 500;
		final int step = 6;
		long start = System.currentTimeMillis();
		final CountDownLatch latch = new CountDownLatch(processors << step);
		final AtomicLong count = new AtomicLong();
		
		for (int i = 0; i < (processors << step); i++) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    for (int i = 0; i < t; i++) {
                        try {
                        	Object response = commonClient.call("BAZINGA.NM.DEMOSERVICE.SAYHELLO", "LIYUAN"+i);

                            if (count.getAndIncrement() % 500 == 0) {
                                System.out.println("count=" + count.get());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } catch (Throwable e) {
							e.printStackTrace();
						}
                    }
                    latch.countDown();
                }
            }).start();
        }
        try {
            latch.await();
            System.out.println("count=" + count.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long second = (System.currentTimeMillis() - start) / 1000;
        System.out.println("Request count: " + count.get() + ", time: " + second + " second, qps: " + count.get() / second);

	}

}
