package ru.capjack.csi.transport.netty.common

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame
import ru.capjack.csi.core.ChannelHandler

class WebSocketChannelHandler(
	private val handler: ChannelHandler
) : NettyChannelHandler() {
	
	private val inputBuffer = ByteBufInputByteBuffer()
	
	override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
		ctx.alloc()
		if (msg is BinaryWebSocketFrame) {
			try {
				inputBuffer.bindSource(msg.content())
				handler.handleChannelInput(inputBuffer)
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
		handler.handleChannelClose()
	}
}
