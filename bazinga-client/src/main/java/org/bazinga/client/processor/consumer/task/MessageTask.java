package org.bazinga.client.processor.consumer.task;

import static org.bazinga.common.serialization.SerializerHolder.serializerImpl;
import io.netty.channel.Channel;

import org.bazinga.client.gather.DefaultResultGather;
import org.bazinga.common.message.Response;
import org.bazinga.common.message.ResultMessageWrapper;

/**
 * 消费者端的任务task
 * @author BazingaLyn
 *
 * @time 2016年6月27日
 */
public class MessageTask implements Runnable {
	
	private final Channel channel;
	
	private final Response response;

	public MessageTask(Channel channel, Response response) {
		this.channel = channel;
		this.response = response;
	}

	@Override
	public void run() {
		final Response _response = response;
		_response.result(serializerImpl().readObject(_response.bytes(), ResultMessageWrapper.class));
		_response.bytes(null);
		DefaultResultGather.received(channel, _response);
	}

}
