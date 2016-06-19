package org.bazinga.common.ack;

import org.bazinga.common.message.Acknowledge;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import static org.bazinga.common.serialization.SerializerHolder.serializerImpl;
import static org.bazinga.common.protocol.BazingaProtocol.MAGIC;
import static org.bazinga.common.protocol.BazingaProtocol.ACK;

@ChannelHandler.Sharable
public class AcknowledgeEncoder extends MessageToByteEncoder<Acknowledge> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Acknowledge ack, ByteBuf out) throws Exception {
        byte[] bytes = serializerImpl().writeObject(ack);
        out.writeShort(MAGIC)
                .writeByte(ACK)
                .writeByte(0)
                .writeLong(ack.sequence())
                .writeInt(bytes.length)
                .writeBytes(bytes);
    }
}
