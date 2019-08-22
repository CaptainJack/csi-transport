package ru.capjack.csi.transport.client

import org.w3c.dom.ARRAYBUFFER
import org.w3c.dom.BinaryType
import org.w3c.dom.WebSocket
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener
import ru.capjack.csi.core.client.ConnectionAcceptor
import ru.capjack.tool.lang.EventException
import ru.capjack.tool.logging.ownLogger
import ru.capjack.tool.utils.ErrorCatcher
import ru.capjack.tool.utils.protect

internal class WebSocketConnectionOpener(
	private val id: Any,
	private val errorCatcher: ErrorCatcher,
	private val acceptor: ConnectionAcceptor,
	url: String
) : EventListener {
	
	private val socket = WebSocket(url)
	
	init {
		socket.binaryType = BinaryType.ARRAYBUFFER
		socket.addEventListener("open", this)
		socket.addEventListener("error", this)
		socket.addEventListener("close", this)
	}
	
	override fun handleEvent(event: Event) {
		errorCatcher.protect {
			socket.removeEventListener("open", this)
			socket.removeEventListener("error", this)
			socket.removeEventListener("close", this)
			
			when (event.type) {
				"open"  -> handleOpen()
				"error" -> handleError(event)
				"close" -> handleClose()
			}
		}
	}
	
	private fun handleOpen() {
		WebSocketConnection(id, errorCatcher, socket, acceptor)
	}
	
	private fun handleClose() {
		ownLogger.warn("[$id] Connection closed on opening")
		acceptor.acceptFail()
	}
	
	private fun handleError(event: Event) {
		ownLogger.warn("[$id] Connection error on opening", EventException(event))
		acceptor.acceptFail()
	}
}