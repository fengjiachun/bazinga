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
 * 第一次性能测试结果： count=128000 Request count: 128000, time: 90 second, qps: 1422
 * 
 * 
 * 第二次：优化日记配置 count=128000 反而变得更慢 (＞﹏＜) Request count: 128000, time: 103 second,
 * qps: 1242
 * 
 * 第三次：引入lockback日记处理，去掉业务过程中多于的logger info级别的信息 
 * 2016-06-28 18:09:03.789 INFO [main] [BenchmarkClient] - count=128000 
 * 2016-06-28 18:09:03.789 INFO [main] [BenchmarkClient] - Request count: 128000, time: 5 second, qps: 25600
 * 
 * 2016-07-11 14:23:13.728 INFO [main] [BenchmarkClient] - count=25600000
 * 2016-07-11 14:23:13.728 INFO [main] [BenchmarkClient] - Request count:25600000, time: 585 second, qps: 43760
 * 
 * 2016-07-13 11:39:12.990 INFO  [main] [BenchmarkClient] - count=12800000
 * 2016-07-13 11:39:12.990 INFO  [main] [BenchmarkClient] - Request count: 12800000, time: 319 second, qps: 40125
 * 
 * 2016-07-13 13:27:53.224 INFO  [main] [BenchmarkClient] - count=128000000
 * 2016-07-13 13:27:53.224 INFO  [main] [BenchmarkClient] - Request count: 128000000, time: 3024 second, qps: 42328
 * 
 */
public class BenchmarkClient {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(BenchmarkClient.class);

	private final static int WRITE_BUFFER_HIGH_WATER_MARK = 256 * 1024;

	private final static int WRITE_BUFFER_LOW_WATER_MARK = 128 * 1024;

	public static void main(String[] args) throws Exception {

		int processors = Runtime.getRuntime().availableProcessors();

		SubScribeInfo info = new SubScribeInfo();
		List<String> servicesNames = new ArrayList<String>();
		
		servicesNames.add("BAZINGA.NM.DEMOSERVICE.SAYHELLO");
		info.setServiceNames(servicesNames);
		final CommonClient commonClient = new CommonClient(info, WRITE_BUFFER_HIGH_WATER_MARK, WRITE_BUFFER_LOW_WATER_MARK);
		//remote 172.31.2.206
		commonClient.connectToRegistryServer(8080, "127.0.0.1");

		/**
		 * 因为这是性能极限测试，TCP协议是慢启动的，滑窗的大小是逐渐变大，并且趋于稳定的，
		 * 所以这边先跑10000次，使得下面的多线程的测试一开始就能以饱和的Congestion Window
		 * 状态运行
		 */
		for (int i = 0; i < 100000; i++) {
			try {
				commonClient.call("BAZINGA.NM.DEMOSERVICE.SAYHELLO", "bazinga");
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		
		/****
		 * 
		 *  真实的生产环境下，服务提供者会根据自己的能力给出自己单台服务器的链接数，因为在服务端的netty模型中，worker的线程数是Available CPU<<1
		 *  消费端对单台服务就去连接消费端available CPU的个数去连接
		 *  这样测试的性能最高
		 * UnresolvedAddress[] addresses = new UnresolvedAddress[processors];
         * for (int i = 0; i < processors; i++) {
         *  addresses[i] = new UnresolvedAddress("127.0.0.1", 18099);
         *   connector.connect(addresses[i]);
         * }
		 * 
		 */

		final int t = 500000;
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
							commonClient.call("BAZINGA.NM.DEMOSERVICE.SAYHELLO", "bazinga");

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
