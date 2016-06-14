package org.bazinga.client.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import static org.bazinga.common.protocol.BazingaProtocol.MAGIC;
import static org.bazinga.common.protocol.BazingaProtocol.RESPONSE;

import org.bazinga.common.message.Response;

public class ResponseEncoder extends MessageToByteEncoder<Response> {
	
	
	@Override
	protected void encode(ChannelHandlerContext ctx, Response msg, ByteBuf out)
			throws Exception {
		doEncodeResponse(msg, out);
	}

	private void doEncodeResponse(Response response, ByteBuf out) {
		
		byte[] bytes = response.bytes();
		out.writeShort(MAGIC)
        .writeByte(RESPONSE)
        .writeByte(response.status())
        .writeLong(response.id())
        .writeInt(bytes.length)
        .writeBytes(bytes);
	}

}
