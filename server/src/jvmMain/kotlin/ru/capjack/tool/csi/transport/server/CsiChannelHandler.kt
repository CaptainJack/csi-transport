package ru.capjack.tool.csi.transport.server

import io.netty.buffer.ByteBuf
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame
import io.netty.util.concurrent.GenericFutureListener
import ru.capjack.tool.csi.core.Connection
import ru.capjack.tool.csi.core.server.ConnectionAcceptor
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.io.readToArray
import java.util.concurrent.atomic.AtomicInteger

internal class CsiChannelHandler(
	acceptor: ConnectionAcceptor,
	channel: Channel
) : ChannelInboundHandlerAdapter() {
	
	private val handler = acceptor.acceptConnection(ChannelConnection(channel))
	
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
	
	private class ChannelConnection(
		private val channel: Channel
	) : Connection, GenericFutureListener<ChannelFuture> {
		
		@Volatile
		private var closed: Boolean = false
		private var sendCounter = AtomicInteger()
		
		override val id: Any
			get() = channel.id()
		
		override fun close() {
			closed = true
			if (sendCounter.get() == 0) {
				doClose()
			}
		}
		
		override fun send(data: Byte) {
			send(1) {
				writeByte(data.toInt() and 0xFF)
			}
		}
		
		override fun send(data: ByteArray) {
			send(data.size) {
				writeBytes(data)
			}
		}
		
		override fun send(data: InputByteBuffer) {
			send(data.readableSize) {
				writeBytes(data.readToArray())
			}
		}
		
		override fun operationComplete(future: ChannelFuture) {
			// After writeAndFlush
			if (sendCounter.getAndDecrement() == 1 && closed) {
				doClose()
			}
		}
		
		private fun doClose() {
			channel.close()
		}
		
		private inline fun send(size: Int, fill: ByteBuf.() -> Unit) {
			send(allocBuffer(size).apply(fill))
		}
		
		private fun send(buffer: ByteBuf) {
			sendCounter.getAndIncrement()
			channel.writeAndFlush(BinaryWebSocketFrame(buffer)).addListener(this)
		}
		
		private fun allocBuffer(size: Int): ByteBuf {
			return channel.alloc().buffer(size, size)
		}
	}
}


