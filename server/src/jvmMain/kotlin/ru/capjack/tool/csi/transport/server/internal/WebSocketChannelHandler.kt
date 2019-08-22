package ru.capjack.tool.csi.transport.server.internal

import io.netty.buffer.ByteBuf
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame
import ru.capjack.tool.csi.core.server.ConnectionAcceptor
import ru.capjack.tool.logging.ownLogger
import java.io.IOException

internal class WebSocketChannelHandler(
	acceptor: ConnectionAcceptor,
	channel: Channel
) : ChannelInboundHandlerAdapter(), (Channel, ByteBuf) -> ChannelFuture {
	private val handler = acceptor.acceptConnection(ChannelConnection(channel, this))
	
	private val inputBuffer = ByteBufAsInputByteBufferView()
	
	override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
		if (msg is BinaryWebSocketFrame) {
			try {
				inputBuffer.bindSource(msg.content())
				handler.handleInput(inputBuffer)
			}
			finally {
				inputBuffer.releaseSource()
				msg.release()
			}
		}
		else {
			super.channelRead(ctx, msg)
		}
	}
	
	override fun handlerRemoved(ctx: ChannelHandlerContext?) {
		handler.handleClose()
	}
	
	override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
		if (cause !is IOException || cause.message != "Connection reset by peer") {
			ownLogger.warn("Uncaught exception", cause)
		}
		handler.handleClose()
		ctx.close()
	}
	
	
	override fun invoke(channel: Channel, buffer: ByteBuf): ChannelFuture {
		if (buffer.readableBytes() <= MAX_FRAME_SIZE) {
			return channel.writeAndFlush(BinaryWebSocketFrame(buffer))
		}
		
		try {
			while (true) {
				var size = buffer.readableBytes()
				var last = false
				if (size > MAX_FRAME_SIZE) {
					size = MAX_FRAME_SIZE
				}
				else {
					last = true
				}
				val frame = BinaryWebSocketFrame(channel.alloc().buffer(size, size).writeBytes(buffer, size))
				
				if (last) {
					return channel.writeAndFlush(frame)
				}
				else {
					channel.write(frame)
				}
			}
		}
		finally {
			buffer.release()
		}
	}
	
	private companion object {
		const val MAX_FRAME_SIZE = 16384
	}
}
