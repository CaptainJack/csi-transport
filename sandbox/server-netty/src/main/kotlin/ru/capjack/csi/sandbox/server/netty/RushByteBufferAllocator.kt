package ru.capjack.csi.sandbox.server.netty

import ru.capjack.tool.io.ArrayByteBuffer
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.utils.concurrency.ObjectAllocator
import java.util.concurrent.atomic.AtomicInteger

class RushByteBufferAllocator : ObjectAllocator<ByteBuffer> {
	
	val produces = AtomicInteger()
	val clears = AtomicInteger()
	val disposes = AtomicInteger()
	
	override fun produceInstance(): ByteBuffer {
		produces.getAndIncrement()
		return ArrayByteBuffer()
	}
	
	override fun clearInstance(instance: ByteBuffer) {
		clears.getAndIncrement()
		instance.clear()
	}
	
	override fun disposeInstance(instance: ByteBuffer) {
		disposes.getAndIncrement()
		instance.clear()
	}
}
