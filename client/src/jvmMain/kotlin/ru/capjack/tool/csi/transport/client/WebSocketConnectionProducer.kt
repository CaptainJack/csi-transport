package ru.capjack.tool.csi.transport.client

import ru.capjack.tool.csi.core.client.ConnectionAcceptor
import ru.capjack.tool.csi.core.client.ConnectionProducer

class WebSocketConnectionProducer(address: String, secure: Boolean) : ConnectionProducer {
	override fun produceConnection(acceptor: ConnectionAcceptor) {
		TODO("not implemented")
	}
}