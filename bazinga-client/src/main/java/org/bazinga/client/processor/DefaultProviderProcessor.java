package org.bazinga.client.processor;

import java.util.concurrent.Executor;

import org.bazinga.client.executor.ExecutorFactory;
import org.bazinga.client.executor.ProviderExecutorFactory;
import org.bazinga.client.processor.task.MessageTask;
import org.bazinga.client.provider.DefaultProvider;
import org.bazinga.common.message.Request;
import org.bazinga.common.spi.BazingaServiceLoader;

import io.netty.channel.Channel;

public class DefaultProviderProcessor extends AbstractProviderProcessor{
	
	public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
	
	private DefaultProvider defaultProvider;
	
	private final Executor executor;
	
	DefaultProviderProcessor(DefaultProvider defaultProvider){
		this.defaultProvider = defaultProvider;
		
		ExecutorFactory factory = (ExecutorFactory) BazingaServiceLoader.load(ProviderExecutorFactory.class);
        executor = factory.newExecutor(AVAILABLE_PROCESSORS << 1);
		
	}

	@Override
	public void handleRequest(Channel channel, Request request) throws Exception {
		MessageTask task = new MessageTask(this, channel, request);
        if (executor == null) {
            task.run();
        } else {
            executor.execute(task);
        }
	}
	
	

}
