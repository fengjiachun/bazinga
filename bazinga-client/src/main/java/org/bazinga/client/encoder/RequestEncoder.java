package org.bazinga.client.encoder;

import static org.bazinga.common.protocol.BazingaProtocol.MAGIC;
import static org.bazinga.common.protocol.BazingaProtocol.REQUEST;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import org.bazinga.common.message.Request;

public class RequestEncoder extends MessageToByteEncoder<Request>{

	@Override
	protected void encode(ChannelHandlerContext ctx, Request msg, ByteBuf out)
			throws Exception {
		doEncodeRequest(msg, out);
	}

	private void doEncodeRequest(Request request, ByteBuf out) {
		 byte[] bytes = request.bytes();

	        out.writeShort(MAGIC)
	                .writeByte(REQUEST)
	                .writeByte(0x00)
	                .writeLong(request.invokeId())
	                .writeInt(bytes.length)
	                .writeBytes(bytes);
	}

}
