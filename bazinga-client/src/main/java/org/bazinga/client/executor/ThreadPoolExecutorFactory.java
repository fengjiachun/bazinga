package org.bazinga.client.executor;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.bazinga.client.processor.RejectedTaskPolicyWithReport;
import org.bazinga.common.utils.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadPoolExecutorFactory implements ExecutorFactory {
	
	protected static final Logger logger = LoggerFactory.getLogger(ThreadPoolExecutorFactory.class);

	@Override
	public Executor newExecutor(int parallelism) {
		
		BlockingQueue<Runnable> workQueue = null;
		
		workQueue = new ArrayBlockingQueue<Runnable>(32768);
		
		 return new ThreadPoolExecutor(
                parallelism,
                512,
                120L,
                SECONDS,
                workQueue,
                new NamedThreadFactory("processor"),
                createRejectedPolicy());
	}

	private RejectedExecutionHandler createRejectedPolicy() {
		
		RejectedExecutionHandler handler = new RejectedTaskPolicyWithReport("processor");
		return handler;
	}
	

}
