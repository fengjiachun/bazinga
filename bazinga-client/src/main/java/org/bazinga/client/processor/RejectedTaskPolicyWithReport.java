package org.bazinga.client.processor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.bazinga.common.logger.InternalLogger;
import org.bazinga.common.logger.InternalLoggerFactory;

public class RejectedTaskPolicyWithReport implements RejectedExecutionHandler {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(RejectedTaskPolicyWithReport.class);

    private final String threadPoolName;

    public RejectedTaskPolicyWithReport(String threadPoolName) {
        this.threadPoolName = threadPoolName;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {

        logger.error("Thread pool [{}] is exhausted! {}.", threadPoolName, e.toString());

        if (r instanceof RejectedRunnable) {
            ((RejectedRunnable) r).rejected(); // 交给用户来处理
        } else {
            if (!e.isShutdown()) {
                BlockingQueue<Runnable> queue = e.getQueue();
                int discardSize = queue.size() >> 1;
                for (int i = 0; i < discardSize; i++) {
                    queue.poll();
                }

                try {
                    queue.put(r);
                } catch (InterruptedException ignored) { /* should not be interrupted */ }
            }
        }
    }
}
