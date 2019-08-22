package ru.capjack.tool.csi.transport.server.internal

import io.netty.buffer.ByteBuf
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.util.concurrent.GenericFutureListener
import ru.capjack.tool.csi.core.Connection
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.io.readToArray
import java.util.concurrent.atomic.AtomicInteger

internal class ChannelConnection(
	private val channel: Channel,
	private val sender: (Channel, ByteBuf) -> ChannelFuture
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
		val size = data.readableSize
		val arrayView = data.readableArrayView
		send(size) {
			writeBytes(arrayView.array, arrayView.readerIndex, size)
		}
		arrayView.commitRead(size)
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
	
	private fun allocBuffer(size: Int): ByteBuf {
		return channel.alloc().buffer(size, size)
	}
	
	private fun send(buffer: ByteBuf) {
		sendCounter.getAndIncrement()
		sender(channel, buffer).addListener(this)
	}
}
