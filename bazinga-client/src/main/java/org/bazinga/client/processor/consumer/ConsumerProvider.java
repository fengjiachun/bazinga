package org.bazinga.client.processor.consumer;

import io.netty.channel.Channel;

import org.bazinga.common.message.Response;

/**
 * 消费者端的处理器
 * @author BazingaLyn
 *
 * @time 2016年6月27日
 */
public interface ConsumerProvider {
	
	/**
	 * 处理器提供者端反馈出的响应信息
	 * @param channel
	 * @param response
	 * @throws Exception
	 */
	void handleResponse(Channel channel, Response response) throws Exception;

}
