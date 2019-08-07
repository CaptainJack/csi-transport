package ru.capjack.tool.csi.transport.server

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame

@Sharable
class WebSocketChannelCodec : ChannelDuplexHandler() {
	override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
		val content = (msg as BinaryWebSocketFrame).content()
		try {
			val size = content.readableBytes()
			ctx.fireChannelRead(ctx.alloc().buffer(size, size).writeBytes(content))
		}
		finally {
			msg.release()
		}
	}
	
	override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
		val size = (msg as ByteBuf).readableBytes()
		try {
			super.write(ctx, BinaryWebSocketFrame(ctx.alloc().buffer(size, size).writeBytes(msg)), promise)
		}
		finally {
			msg.release()
		}
	}
}
