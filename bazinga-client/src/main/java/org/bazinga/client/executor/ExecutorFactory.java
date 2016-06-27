package org.bazinga.client.executor;

import java.util.concurrent.Executor;

/**
 *线程工厂
 * @author BazingaLyn
 *
 * @time
 */
public interface ExecutorFactory extends ProviderExecutorFactory,ConsumerExecutorFactory {
	
	Executor newExecutor(int parallelism);

}
