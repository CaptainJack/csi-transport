package ru.capjack.csi.transport.netty.common

import io.netty.buffer.ByteBuf
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame

class WebSocketChannelWriter : ChannelWriter {
	private companion object {
		const val MAX_FRAME_SIZE = 16384
	}
	
	override fun writeAndFlush(channel: Channel, message: ByteBuf): ChannelFuture {
		if (message.readableBytes() <= MAX_FRAME_SIZE) {
			return channel.writeAndFlush(BinaryWebSocketFrame(message))
		}
		
		try {
			while (true) {
				var size = message.readableBytes()
				var last = false
				if (size > MAX_FRAME_SIZE) {
					size = MAX_FRAME_SIZE
				}
				else {
					last = true
				}
				val frame = BinaryWebSocketFrame(channel.alloc().buffer(size, size).writeBytes(message, size))
				
				if (last) {
					return channel.writeAndFlush(frame)
				}
				else {
					channel.write(frame)
				}
			}
		}
		finally {
			message.release()
		}
	}
}