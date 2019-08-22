package ru.capjack.csi.transport.common

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame
import ru.capjack.csi.core.ConnectionHandler

class WebSocketChannelHandler(
	private val handler: ConnectionHandler
) : AbstractChannelHandler() {
	
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
	
	override fun channelUnregistered(ctx: ChannelHandlerContext) {
		handler.handleClose()
	}
	
	override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
		@Suppress("DEPRECATION")
		super.exceptionCaught(ctx, cause)
		ctx.close()
	}
}
