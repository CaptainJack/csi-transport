package ru.capjack.csi.transport.common

import io.netty.buffer.ByteBuf
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.util.concurrent.GenericFutureListener
import ru.capjack.csi.core.Connection
import ru.capjack.tool.io.InputByteBuffer

class ChannelConnection(
	private val channel: Channel,
	private val sender: MessageSender
) : Connection, GenericFutureListener<ChannelFuture> {
	
	private val lock = Any()
	
	@Volatile
	private var closed: Boolean = false
	@Volatile
	private var sending = false
	@Volatile
	private var sendingBuffer:ByteBuf? = null
	
	override val id: Any
		get() = channel.id()
	
	override fun close() {
		val toClose: Boolean
		
		synchronized(lock) {
			closed = true
			toClose = !sending
		}
		
		if (toClose) {
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
		
		val b: ByteBuf?
		var toClose = false
		
		synchronized(lock) {
			b = sendingBuffer
			if (b == null) {
				sending = false
				if (closed) {
					toClose = true
				}
			}
			else {
				sendingBuffer = null
			}
		}
		
		if (toClose) {
			doClose()
		}
		else if (b != null) {
			sender.sendMessage(channel, b).addListener(this)
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
		synchronized(lock) {
			when {
				sending -> {
					var b = sendingBuffer
					if (b == null) {
						val size = buffer.readableBytes()
						b = channel.alloc().buffer(size)!!
						sendingBuffer = b
					}
					b.writeBytes(buffer)
					buffer.release()
					return
				}
				closed  -> {
					buffer.release()
					return
				}
				else    -> sending = true
			}
		}
		sender.sendMessage(channel, buffer).addListener(this)
	}
}
