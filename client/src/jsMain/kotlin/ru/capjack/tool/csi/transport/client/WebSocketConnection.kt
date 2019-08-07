package ru.capjack.tool.csi.transport.client

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.w3c.dom.MessageEvent
import org.w3c.dom.WebSocket
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener
import ru.capjack.tool.csi.core.Connection
import ru.capjack.tool.csi.core.client.ConnectionAcceptor
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.io.readToArray
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
	private val inputBuffer = ByteBuffer(0)
	
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
		socket.send(byteArrayOf(data).unsafeCast<Int8Array>())
	}
	
	override fun send(data: ByteArray) {
		val size = data.size
		val array = data.unsafeCast<Int8Array>()
		if (size <= MAX_FRAME_SIZE) {
			socket.send(array)
		}
		else {
			var offset = 0
			do {
				val nextOffset = offset + MAX_FRAME_SIZE
				socket.send(array.subarray(offset, nextOffset))
				offset = nextOffset
			}
			while (offset < size)
		}
	}
	
	override fun send(data: InputByteBuffer) {
		send(data.readToArray())
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
		val emptyMemory = inputBuffer.memory
		inputBuffer.memory = Int8Array(event.data.unsafeCast<ArrayBuffer>()).unsafeCast<ByteArray>()
		try {
			handler.handleInput(inputBuffer)
		}
		finally {
			inputBuffer.memory = emptyMemory
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
	
	private companion object {
		const val MAX_FRAME_SIZE = 32768
	}
}