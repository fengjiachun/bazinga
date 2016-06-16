package org.bazinga.client.handler;

import static org.bazinga.common.serialization.SerializerHolder.serializerImpl;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import org.bazinga.common.message.Request;
import org.bazinga.common.message.Response;
import org.bazinga.common.message.ResultMessageWrapper;
import org.bazinga.common.message.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@ChannelHandler.Sharable
public class ProviderHandler extends ChannelInboundHandlerAdapter {

	protected static final Logger logger = LoggerFactory
			.getLogger(ProviderHandler.class);

//	private static final AtomicInteger channelCounter = new AtomicInteger(0);

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {

		Channel channel = ctx.channel();
		logger.info("from {} received msg {}", channel.remoteAddress(), msg);

		if (msg instanceof Request) {
			Request request = (Request) msg;

			ResultMessageWrapper messageWrapper = new ResultMessageWrapper();

			messageWrapper.setResult("Test result");

			byte[] results = serializerImpl().writeObject(messageWrapper);
			Response response = Response.newInstance(request.invokeId(),
					Status.OK, results);

			channel.writeAndFlush(response);

		}

		ReferenceCountUtil.release(msg);

	}

}
