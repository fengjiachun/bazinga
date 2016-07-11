package org.bazinga.example.benchmark;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import org.bazinga.client.common.utils.CommonClient;
import org.bazinga.common.logger.InternalLogger;
import org.bazinga.common.logger.InternalLoggerFactory;
import org.bazinga.common.message.SubScribeInfo;

/**
 * 第一次性能测试结果：
 * count=128000
 * Request count: 128000, time: 90 second, qps: 1422
 * 
 * 
 * 第二次：优化日记配置
 * count=128000 反而变得更慢 (＞﹏＜)
 * Request count: 128000, time: 103 second, qps: 1242
 * 
 * 第三次：引入lockback日记处理，去掉业务过程中多于的logger info级别的信息
 * 2016-06-28 18:09:03.789 INFO  [main] [BenchmarkClient] - count=128000
 * 2016-06-28 18:09:03.789 INFO  [main] [BenchmarkClient] - Request count: 128000, time: 5 second, qps: 25600
 * 
 * 2016-07-11 14:23:13.728 INFO  [main] [BenchmarkClient] - count=25600000
 * 2016-07-11 14:23:13.728 INFO  [main] [BenchmarkClient] - Request count: 25600000, time: 585 second, qps: 43760
 * 
 */
public class BenchmarkClient {
	
	private static final InternalLogger logger = InternalLoggerFactory.getInstance(BenchmarkClient.class);
	
	private final static int WRITE_BUFFER_HIGH_WATER_MARK = 256 * 1024;

	private final static int WRITE_BUFFER_LOW_WATER_MARK = 128 * 1024;

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
		final CommonClient commonClient = new CommonClient(info,WRITE_BUFFER_HIGH_WATER_MARK,WRITE_BUFFER_LOW_WATER_MARK);
		commonClient.connectToRegistryServer(port, "127.0.0.1");

		try {
			Object response = commonClient.call("BAZINGA.NM.DEMOSERVICE.SAYHELLO", "LIYUAN");
			if (null != response) {
				if (response instanceof String) {
					logger.info("================" + (String) response);
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

		final int t = 500000;
		final int step = 5;
		long start = System.currentTimeMillis();
		final CountDownLatch latch = new CountDownLatch(processors << step);
		final AtomicLong count = new AtomicLong();
		
		for (int i = 0; i < (processors << step); i++) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    for (int i = 0; i < t; i++) {
                        try {
                        	Object response = commonClient.call("BAZINGA.NM.DEMOSERVICE.SAYHELLO", "jupiter");

                            if (count.getAndIncrement() % 50000 == 0) {
                                logger.info("count=" + count.get());
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
            logger.info("count=" + count.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long second = (System.currentTimeMillis() - start) / 1000;
        logger.info("Request count: " + count.get() + ", time: " + second + " second, qps: " + count.get() / second);

	}

}
