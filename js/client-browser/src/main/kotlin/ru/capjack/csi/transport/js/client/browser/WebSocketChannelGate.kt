package ru.capjack.csi.transport.js.client.browser

import org.w3c.dom.WebSocket
import ru.capjack.csi.core.client.ChannelAcceptor
import ru.capjack.csi.core.client.ChannelGate
import ru.capjack.tool.lang.asThrowable
import ru.capjack.tool.logging.ownLogger

class WebSocketChannelGate(
	private val url: String
) : ChannelGate {
	override fun openChannel(acceptor: ChannelAcceptor) {
		ownLogger.debug("Connect to $url")
		try {
			WebSocketChannelProducer(acceptor, WebSocket(url))
		}
		catch (e: dynamic) {
			ownLogger.warn("WebSocket opening failed", asThrowable(e))
			acceptor.acceptFail()
		}
	}
}