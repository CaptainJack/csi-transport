package ru.capjack.csi.transport.js.client.browser

import org.w3c.dom.ARRAYBUFFER
import org.w3c.dom.BinaryType
import org.w3c.dom.WebSocket
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener
import ru.capjack.csi.core.client.ChannelAcceptor
import ru.capjack.tool.lang.EventException
import ru.capjack.tool.lang.asThrowable
import ru.capjack.tool.logging.ownLogger

internal class WebSocketChannelProducer(
	private val acceptor: ChannelAcceptor,
	private val socket: WebSocket
) : EventListener {
	
	init {
		socket.binaryType = BinaryType.ARRAYBUFFER
		socket.addEventListener("open", this)
		socket.addEventListener("error", this)
		socket.addEventListener("close", this)
	}
	
	override fun handleEvent(event: Event) {
		try {
			socket.removeEventListener("open", this)
			socket.removeEventListener("error", this)
			socket.removeEventListener("close", this)
			
			when (event.type) {
				"open"  -> handleOpen()
				"error" -> handleError(event)
				"close" -> handleClose()
				else    -> throw IllegalStateException("Unexpected event type '${event.type}'")
			}
		}
		catch (e: dynamic) {
			ownLogger.error("Error on handle WebSocket event", asThrowable(e))
			acceptor.acceptFail()
		}
	}
	
	private fun handleOpen() {
		WebSocketChannel(socket, acceptor)
	}
	
	private fun handleClose() {
		ownLogger.warn("WebSocket closed on opening")
		acceptor.acceptFail()
	}
	
	private fun handleError(event: Event) {
		ownLogger.warn("WebSocket error on opening", EventException(event))
		acceptor.acceptFail()
	}
}