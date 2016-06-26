package org.bazinga.client.executor;

import java.util.concurrent.Executor;

public interface ExecutorFactory extends ProviderExecutorFactory,ConsumerExecutorFactory {
	
	Executor newExecutor(int parallelism);

}
