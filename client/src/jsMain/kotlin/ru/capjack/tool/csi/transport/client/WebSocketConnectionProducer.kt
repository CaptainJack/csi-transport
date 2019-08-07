package ru.capjack.tool.csi.transport.client

import ru.capjack.tool.csi.core.client.ConnectionAcceptor
import ru.capjack.tool.csi.core.client.ConnectionProducer
import ru.capjack.tool.lang.asThrowable
import ru.capjack.tool.lang.make
import ru.capjack.tool.logging.ownLogger
import ru.capjack.tool.utils.ErrorCatcher

class WebSocketConnectionProducer(
	private val errorCatcher: ErrorCatcher,
	address: String,
	secure: Boolean
) : ConnectionProducer {
	
	private val url = "${secure.make("wss", "ws")}://$address"
	
	private var connectionCounter = 0
	
	override fun produceConnection(acceptor: ConnectionAcceptor) {
		val id = (++connectionCounter).toString()
		ownLogger.info("[$id] Connect to $url")
		try {
			WebSocketConnectionOpener(id, errorCatcher, acceptor, url)
		}
		catch (e: dynamic) {
			ownLogger.warn("[$id] Connection opening failed", asThrowable(e))
			acceptor.acceptFail()
		}
	}
}
