package ru.capjack.tool.csi.transport.server

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.io.OutputByteBuffer

class ByteBufAsInputByteBufferView : InputByteBuffer {
	private var source: ByteBuf = Unpooled.EMPTY_BUFFER
	
	override val readable: Boolean
		get() = source.isReadable
	
	override val readableSize: Int
		get() = source.readableBytes()
	
	override fun isReadable(size: Int): Boolean {
		return source.isReadable(size)
	}
	
	override fun readArray(target: ByteArray, offset: Int, size: Int) {
		source.readBytes(target, offset, size)
	}
	
	override fun readBuffer(target: OutputByteBuffer, size: Int) {
		if (source.hasArray()) {
			target.writeArray(source.array(), source.arrayOffset() + source.readerIndex(), size)
			source.skipBytes(size)
		}
		else {
			val array = ByteArray(size)
			readArray(array)
			target.writeArray(array)
		}
	}
	
	override fun readByte(): Byte {
		return source.readByte()
	}
	
	override fun readInt(): Int {
		return source.readInt()
	}
	
	override fun readLong(): Long {
		return source.readLong()
	}
	
	override fun readSkip(size: Int) {
		source.skipBytes(size)
	}
	
	fun bindSource(source: ByteBuf) {
		this.source = source
	}
	
	fun releaseSource() {
		this.source = Unpooled.EMPTY_BUFFER
	}
}
