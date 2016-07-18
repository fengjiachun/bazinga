package org.bazinga.client.processor.provider;

import io.netty.channel.Channel;

import org.bazinga.common.message.Request;

/**
 * 提供者的处理器
 * @author BazingaLyn
 * @copyright fjc
 * @time 2016年6月25日
 */
public interface ProviderProcessor extends LookupService {

	
	/**
     * 处理正常请求
     */
    void handleRequest(Channel channel, Request request) throws Exception;

    /**
     * 处理异常
     */
    void handleException(Channel channel, Request request, Throwable cause);
}
