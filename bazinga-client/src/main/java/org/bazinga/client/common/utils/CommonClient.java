package org.bazinga.client.common.utils;

import static org.bazinga.common.serialization.SerializerHolder.serializerImpl;
import io.netty.util.internal.ConcurrentSet;

import org.bazinga.client.comsumer.DefaultConsumer;
import org.bazinga.client.dispatcher.DefaultDispatcher;
import org.bazinga.client.gather.DefaultResultGather;
import org.bazinga.common.exception.NoServiceException;
import org.bazinga.common.message.Request;
import org.bazinga.common.message.RequestMessageWrapper;
import org.bazinga.common.message.SubScribeInfo;
import org.bazinga.common.message.WeightChannel;

public class CommonClient extends DefaultConsumer implements DefaultCommonClient {
	

	public CommonClient(SubScribeInfo info) {
		super(info);
	}

	public Object call(String serviceName, Object... args) throws Throwable {
		
		if(null == serviceName || serviceName.length() == 0){
			throw new NoServiceException("调用的服务名不能为空");
		}
		ConcurrentSet<WeightChannel> channels = getAllMatchedChannel(serviceName);
		if(channels.size() == 0){
			throw new NoServiceException("没有第三方提供该服务，请检查服务名");
		}
		
		final Request request = new Request();
		RequestMessageWrapper message = new RequestMessageWrapper(serviceName,args);
		request.setMessageWrapper(message);
		request.bytes(serializerImpl().writeObject(message));
		WeightChannel weightChannel = loadBalance(channels);
		
		//TODO 每个方法特殊的超时时间没有设置
		DefaultResultGather defaultResultGather = new DefaultDispatcher().dispatcher(weightChannel.getChannel(), request);
		
		return defaultResultGather.getResult();
		
	}

}
