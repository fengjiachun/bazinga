package org.bazinga.client.processor.provider;

import io.netty.channel.Channel;

import java.util.concurrent.Executor;

import org.bazinga.client.executor.ExecutorFactory;
import org.bazinga.client.executor.ProviderExecutorFactory;
import org.bazinga.client.processor.provider.task.MessageTask;
import org.bazinga.client.provider.DefaultProvider;
import org.bazinga.client.provider.model.ServiceWrapper;
import org.bazinga.common.message.Request;
import org.bazinga.common.spi.BazingaServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认的提供端的请求处理器
 * 封装一个task{@link MessageTask}并使用注入的executor来处理请求
 * @author BazingaLyn
 *
 * @time
 */
public class DefaultProviderProcessor extends AbstractProviderProcessor {
	
	protected static final Logger logger = LoggerFactory.getLogger(DefaultProviderProcessor.class);
	
	public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
	
	private DefaultProvider defaultProvider;
	
	private final Executor executor;
	
	public DefaultProviderProcessor(DefaultProvider defaultProvider){
		this.defaultProvider = defaultProvider;
		
		ExecutorFactory factory = (ExecutorFactory) BazingaServiceLoader.load(ProviderExecutorFactory.class);
        executor = factory.newExecutor(AVAILABLE_PROCESSORS << 1);
		
	}

	@Override
	public void handleRequest(Channel channel, Request request) throws Exception {
		MessageTask task = new MessageTask(this, channel, request);
        if (executor == null) {
        	logger.warn("use netty thread handler request");
            task.run();
        } else {
        	logger.info("user custom executor execute request");
            executor.execute(task);
        }
	}

	/**
	 * 根据serviceName
	 */
	@Override
	public ServiceWrapper lookupService(String serviceName) {
		return defaultProvider.getProviderContainer().lookupService(serviceName);
	}
	
	

}
