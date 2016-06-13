package org.bazinga.client.comsumer;

import static org.bazinga.common.protocol.BazingaProtocol.ACK;
import static org.bazinga.common.protocol.BazingaProtocol.MAGIC;
import static org.bazinga.common.protocol.BazingaProtocol.OFFLINE_NOTICE;
import static org.bazinga.common.protocol.BazingaProtocol.PUBLISH_SERVICE;
import static org.bazinga.common.protocol.BazingaProtocol.SUBSCRIBE_SERVICE;
import static org.bazinga.common.serialization.SerializerHolder.serializerImpl;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

import org.bazinga.common.exception.BazingaException;
import org.bazinga.common.message.Acknowledge;
import org.bazinga.common.message.Message;
import org.bazinga.common.message.SubScribeInfo;
import org.bazinga.common.protocol.BazingaProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultConsumer {

	protected static final Logger logger = LoggerFactory
			.getLogger(DefaultConsumer.class);

	private SubScribeInfo info;

	private MessageEncoder messageEncoder = new MessageEncoder();

	private ConsumerHandler consumerHandler = new ConsumerHandler();

	public DefaultConsumer(SubScribeInfo info) {
		this.info = info;
	}

	public void connect(int port, String host) throws Exception {

		EventLoopGroup group = new NioEventLoopGroup();
		ChannelFuture future = null;
		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class)
					.option(ChannelOption.TCP_NODELAY, true)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch)
								throws Exception {
							ch.pipeline().addLast("encoder", messageEncoder);
							ch.pipeline().addLast("decoder", new MessageDecoder());
							ch.pipeline().addLast(consumerHandler);
						}
					});

			future = b.connect(host, port).sync();
			future.channel().closeFuture().sync();
		} finally {
			group.shutdownGracefully();
		}
	}

	@ChannelHandler.Sharable
	static class MessageEncoder extends MessageToByteEncoder<Message> {

		@Override
		protected void encode(ChannelHandlerContext ctx, Message msg,
				ByteBuf out) throws Exception {
			byte[] bytes = serializerImpl().writeObject(msg);

			out.writeShort(MAGIC).writeByte(msg.sign()).writeByte(0)
					.writeLong(0).writeInt(bytes.length).writeBytes(bytes);
		}
	}

	static class MessageDecoder extends ReplayingDecoder<MessageDecoder.State> {

		/**
		 * 为state()方法中的值赋值
		 */
		public MessageDecoder() {
			super(State.HEADER_MAGIC);
		}

		private final BazingaProtocol header = new BazingaProtocol();

		@Override
		protected void decode(ChannelHandlerContext ctx, ByteBuf in,
				List<Object> out) throws Exception {

			logger.info("/*********************begin decoder****************/");
			switch (state()) {

			case HEADER_MAGIC:
				checkMagic(in.readShort()); // MAGIC
				checkpoint(State.HEADER_SIGN);
			case HEADER_SIGN:
				header.sign(in.readByte()); // 消息标志位
				checkpoint(State.HEADER_STATUS);
			case HEADER_STATUS:
				in.readByte(); // no-op
				checkpoint(State.HEADER_ID);
			case HEADER_ID:
				header.id(in.readLong()); // 消息id
				checkpoint(State.HEADER_BODY_LENGTH);
			case HEADER_BODY_LENGTH:
				header.bodyLength(in.readInt()); // 消息体长度
				checkpoint(State.BODY);
			case BODY:
				switch (header.sign()) {
				case PUBLISH_SERVICE:
				case OFFLINE_NOTICE: {
					byte[] bytes = new byte[header.bodyLength()];
					in.readBytes(bytes);

					Message msg = serializerImpl().readObject(bytes,
							Message.class);
					msg.sign(header.sign());
					out.add(msg);

					break;
				}
				case ACK: {
					byte[] bytes = new byte[header.bodyLength()];
					in.readBytes(bytes);

					Acknowledge ack = serializerImpl().readObject(bytes,
							Acknowledge.class);
					out.add(ack);
					break;
				}
				default:
					break;

				}
				checkpoint(State.HEADER_MAGIC);

			}
		}

		private static void checkMagic(short magic) {
			if (MAGIC != magic) {
				throw new BazingaException();
			}
		}

		enum State {
			HEADER_MAGIC, HEADER_SIGN, HEADER_STATUS, HEADER_ID, HEADER_BODY_LENGTH, BODY
		}
	}

	class ConsumerHandler extends ChannelInboundHandlerAdapter {

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {

			logger.debug("***********consumer has start**************");
			logger.info("consumer msg :{}", info);

			Channel channel = ctx.channel();

			Message message = new Message();

			message.sign(SUBSCRIBE_SERVICE);
			message.data(info);

			channel.writeAndFlush(message).addListener(
					ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
				throws Exception {

			logger.error("consumer Handler occur exception:{}",
					cause.getMessage());

			ctx.channel().close();
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg)
				throws Exception {

			logger.info("comsume received some message from monitor {}",msg);
//			if(msg instanceof Message){
//				Message message = (Message)msg;
//				System.out.println(message);
//			}
		}

	}


}
