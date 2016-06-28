package org.bazinga.client.decoder;

import static org.bazinga.common.protocol.BazingaProtocol.HEARTBEAT;
import static org.bazinga.common.protocol.BazingaProtocol.MAGIC;
import static org.bazinga.common.protocol.BazingaProtocol.RESPONSE;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

import org.bazinga.common.exception.BazingaException;
import org.bazinga.common.logger.InternalLogger;
import org.bazinga.common.logger.InternalLoggerFactory;
import org.bazinga.common.message.Response;
import org.bazinga.common.protocol.BazingaProtocol;

public class ConsumerDecoder extends ReplayingDecoder<ConsumerDecoder.State> {
	
	private static final InternalLogger logger = InternalLoggerFactory.getInstance(ConsumerDecoder.class);
	
	private static final int MAX_BODY_SIZE = 1024 * 1024 * 5;
	
	
	private static final boolean USE_COMPOSITE_BUF = false;
	
	
	
	public ConsumerDecoder() {
		super(State.HEADER_MAGIC);
		if(USE_COMPOSITE_BUF){
			setCumulator(COMPOSITE_CUMULATOR);	
		}
	}

	private final BazingaProtocol header = new BazingaProtocol();

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> out) throws Exception {

		switch (state()) {
		case HEADER_MAGIC:
			checkMagic(in.readShort());         // MAGIC
            checkpoint(State.HEADER_SIGN);
		case HEADER_SIGN:
            header.sign(in.readByte());         // 消息标志位
            checkpoint(State.HEADER_STATUS);
        case HEADER_STATUS:
            header.status(in.readByte());       // 状态位
            checkpoint(State.HEADER_ID);
        case HEADER_ID:
            header.id(in.readLong());           // 消息id
            checkpoint(State.HEADER_BODY_LENGTH);
        case HEADER_BODY_LENGTH:
            header.bodyLength(in.readInt());    // 消息体长度
            checkpoint(State.BODY);
        case BODY:
            switch (header.sign()) {
                case HEARTBEAT:
                    break;
                case RESPONSE: {
                	int bodyLength = checkBodyLength(header.bodyLength());
                    byte[] bytes = new byte[bodyLength];
                    in.readBytes(bytes);
                    out.add(Response.newInstance(header.id(), header.status(), bytes));
                    break;
                }
            }
		default:
			break;
		}
		checkpoint(State.HEADER_MAGIC);
	
	}

	private int checkBodyLength(int bodyLength) {
		if (bodyLength > MAX_BODY_SIZE) {
            throw new BazingaException("body of request is bigger than limit value "+ MAX_BODY_SIZE);
        }
        return bodyLength;
	}

	private void checkMagic(short magic) {
		if (MAGIC != magic) {
			logger.error("Magic is not match");
            throw new BazingaException("magic value is not equal "+MAGIC);
        }
	}
	
	enum State {
        HEADER_MAGIC,
        HEADER_SIGN,
        HEADER_STATUS,
        HEADER_ID,
        HEADER_BODY_LENGTH,
        BODY
    }

}
