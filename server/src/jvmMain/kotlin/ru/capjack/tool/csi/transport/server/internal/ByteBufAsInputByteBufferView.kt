package ru.capjack.tool.csi.transport.server.internal

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.io.OutputByteBuffer
import ru.capjack.tool.io.ensureWriteableArrayView

internal class ByteBufAsInputByteBufferView : InputByteBuffer, InputByteBuffer.ArrayView {
	private var source: ByteBuf = Unpooled.EMPTY_BUFFER
	
	override val readable: Boolean
		get() = source.isReadable
	
	override val readableSize: Int
		get() = source.readableBytes()
	
	override val readableArrayView: InputByteBuffer.ArrayView
		get() = this
	
	override val readerIndex: Int
		get() = source.arrayOffset() + source.readerIndex()
	
	override val array: ByteArray
		get() = if (source.hasArray()) {
			source.array()
		}
		else {
			source.readerIndex().let { index ->
				ByteArray(source.readableBytes()).also {
					source.readBytes(it)
					source.readerIndex(index)
				}
			}
		}
	
	override fun isReadable(size: Int): Boolean {
		return source.isReadable(size)
	}
	
	override fun readArray(target: ByteArray, offset: Int, size: Int) {
		source.readBytes(target, offset, size)
	}
	
	override fun readBuffer(target: OutputByteBuffer, size: Int) {
		if (source.hasArray()) {
			target.writeArray(source.array(), readerIndex, size)
			source.skipBytes(size)
		}
		else {
			target.ensureWriteableArrayView(size).also {
				source.readBytes(it.array, it.writerIndex, size)
				it.commitWrite(size)
			}
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
	
	override fun skipRead(size: Int) {
		source.skipBytes(size)
	}
	
	override fun backRead(size: Int) {
		source.readerIndex(source.readerIndex() - size)
	}
	
	override fun commitRead(size: Int) {
		skipRead(size)
	}
	
	fun bindSource(source: ByteBuf) {
		this.source = source
	}
	
	fun releaseSource() {
		this.source = Unpooled.EMPTY_BUFFER
	}
}
