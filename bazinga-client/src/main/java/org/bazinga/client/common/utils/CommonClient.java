package org.bazinga.client.common.utils;

import static org.bazinga.common.serialization.SerializerHolder.serializerImpl;
import io.netty.channel.Channel;
import io.netty.util.internal.ConcurrentSet;

import org.bazinga.client.comsumer.DefaultConsumer;
import org.bazinga.client.dispatcher.DefaultDispatcher;
import org.bazinga.client.gather.DefaultResultGather;
import org.bazinga.client.loadbalance.LoadBalance;
import org.bazinga.common.exception.BazingaException;
import org.bazinga.common.message.Request;
import org.bazinga.common.message.RequestMessageWrapper;
import org.bazinga.common.message.SubScribeInfo;
import org.bazinga.common.message.WeightChannel;

public class CommonClient extends DefaultConsumer implements DefaultCommonClient,LoadBalance {

	public CommonClient(SubScribeInfo info) {
		super(info);
	}

	public Object call(String serviceName, Object... args) throws Throwable {
		
		if(null == serviceName || serviceName.length() == 0){
			throw new BazingaException("调用的服务名不能为空");
		}
		ConcurrentSet<WeightChannel> channels = getAllMatchedChannel(serviceName);
		if(channels.size() == 0){
			throw new BazingaException("没有第三方提供该服务，请检查服务名");
		}
		
		final Request request = new Request();
		RequestMessageWrapper message = new RequestMessageWrapper(serviceName,args);
		request.setMessageWrapper(message);
		request.bytes(serializerImpl().writeObject(message));
		Channel channel = loadBalance(channels);
		
		//TODO 每个方法特殊的超时时间没有设置
		DefaultResultGather defaultResultGather = new DefaultDispatcher().dispatcher(channel, request);
		
		return defaultResultGather.getResult();
		
	}

	//TODO 先默认返回第一个提供的channel
	public Channel loadBalance(ConcurrentSet<WeightChannel> channels) {
		Channel channel = null;
		for(WeightChannel weightChannel:channels){
			channel =  weightChannel.getChannel();
			break;
		}
		return channel;
	}

}
