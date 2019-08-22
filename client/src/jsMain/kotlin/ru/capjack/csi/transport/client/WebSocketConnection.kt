package ru.capjack.csi.transport.client

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.w3c.dom.MessageEvent
import org.w3c.dom.WebSocket
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener
import ru.capjack.csi.core.Connection
import ru.capjack.csi.core.client.ConnectionAcceptor
import ru.capjack.tool.io.ArrayByteBuffer
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.io.asNative
import ru.capjack.tool.lang.EMPTY_BYTE_ARRAY
import ru.capjack.tool.lang.EventException
import ru.capjack.tool.logging.ownLogger
import ru.capjack.tool.utils.ErrorCatcher
import ru.capjack.tool.utils.protect

internal class WebSocketConnection(
	override val id: Any,
	private val errorCatcher: ErrorCatcher,
	private val socket: WebSocket,
	acceptor: ConnectionAcceptor
) : Connection, EventListener {
	
	private val handler = acceptor.acceptSuccess(this)
	private val inputBuffer = ArrayByteBuffer(0)
	
	init {
		socket.addEventListener("message", this)
		socket.addEventListener("close", this)
		socket.addEventListener("error", this)
	}
	
	override fun close() {
		stopListenSocket()
		socket.close()
	}
	
	override fun send(data: Byte) {
		socket.send(byteArrayOf(data).asNative())
	}
	
	override fun send(data: ByteArray) {
		val size = data.size
		val array = data.asNative()
		if (size > MAX_FRAME_SIZE) {
			sendFrames(size, 0, array)
		}
		else {
			socket.send(array)
		}
	}
	
	override fun send(data: InputByteBuffer) {
		val size = data.readableSize
		val arrayView = data.readableArrayView
		val index = arrayView.readerIndex
		val array = arrayView.array.asNative()
		
		if (size > MAX_FRAME_SIZE) {
			sendFrames(size, index, array)
		}
		else {
			if (index == 0 && size == array.length) {
				socket.send(array)
			}
			else {
				socket.send(array.subarray(index, index + size))
			}
		}
		
		arrayView.commitRead(size)
	}
	
	override fun handleEvent(event: Event) {
		errorCatcher.protect {
			when (event.type) {
				"message" -> handleMassage(event.unsafeCast<MessageEvent>())
				"close"   -> handleClose()
				"error"   -> handleError(event)
			}
		}
	}
	
	private fun handleMassage(event: MessageEvent) {
		inputBuffer.array = Int8Array(event.data.unsafeCast<ArrayBuffer>()).unsafeCast<ByteArray>()
		try {
			handler.handleInput(inputBuffer)
		}
		finally {
			inputBuffer.array = EMPTY_BYTE_ARRAY
		}
	}
	
	private fun handleClose() {
		stopListenSocket()
		handler.handleClose()
	}
	
	private fun handleError(event: Event) {
		ownLogger.warn("[$id] Connection error", EventException(event))
		handleClose()
	}
	
	private fun stopListenSocket() {
		socket.removeEventListener("message", this)
		socket.removeEventListener("close", this)
		socket.removeEventListener("error", this)
	}
	
	private fun sendFrames(size: Int, index: Int, array: Int8Array) {
		var offset = index
		do {
			val nextOffset = (offset + MAX_FRAME_SIZE).coerceAtMost(size)
			socket.send(array.subarray(offset, nextOffset))
			offset = nextOffset
		}
		while (offset < size)
	}
	
	private companion object {
		const val MAX_FRAME_SIZE = 16384
	}
}