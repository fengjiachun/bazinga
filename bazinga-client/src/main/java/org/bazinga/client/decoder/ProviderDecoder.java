package org.bazinga.client.decoder;

import static org.bazinga.common.protocol.BazingaProtocol.HEARTBEAT;
import static org.bazinga.common.protocol.BazingaProtocol.MAGIC;
import static org.bazinga.common.protocol.BazingaProtocol.REQUEST;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

import org.bazinga.common.exception.BazingaException;
import org.bazinga.common.message.Request;
import org.bazinga.common.protocol.BazingaProtocol;
import org.bazinga.common.utils.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 */
public class ProviderDecoder extends ReplayingDecoder<ProviderDecoder.State> {
	
	protected static final Logger logger = LoggerFactory.getLogger(ProviderDecoder.class); 
	
	private static final int MAX_BODY_SIZE = 1024 * 1024 * 5;
	
	
	private static final boolean USE_COMPOSITE_BUF = false;
	
	
	
	public ProviderDecoder() {
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
                case REQUEST: {
                    int bodyLength = checkBodyLength(header.bodyLength());
                    byte[] bytes = new byte[bodyLength];
                    in.readBytes(bytes);
                    Request request = new Request(header.id());
                    request.timestamp(SystemClock.millisClock().now());
                    request.bytes(bytes);
                    out.add(request);
                    break;
                }
            }

		default:
			break;
		}
		checkpoint(State.HEADER_MAGIC);
	}
	
	private static int checkBodyLength(int bodyLength) {
		if (bodyLength > MAX_BODY_SIZE) {
            throw new BazingaException("body of request is bigger than limit value "+ MAX_BODY_SIZE);
        }
        return bodyLength;
	}

	private static void checkMagic(short magic) {
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
