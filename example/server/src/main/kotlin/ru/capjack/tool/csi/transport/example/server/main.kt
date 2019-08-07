package ru.capjack.tool.csi.transport.example.server

import ru.capjack.tool.csi.core.server.Client
import ru.capjack.tool.csi.core.server.ClientAcceptor
import ru.capjack.tool.csi.core.server.ClientAuthorizer
import ru.capjack.tool.csi.core.server.ClientMessageReceiver
import ru.capjack.tool.csi.core.server.Server
import ru.capjack.tool.csi.transport.server.EventLoopGroups
import ru.capjack.tool.csi.transport.server.WebSocketConnectionGateway
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.utils.concurrency.ScheduledExecutorImpl
import java.util.concurrent.TimeUnit

fun main() {
	val elg = EventLoopGroups()
	
	val server = Server(
		ScheduledExecutorImpl(elg.connectionsAsScheduledExecutorService),
		ExampleClientAuthorizer(),
		ExampleClientAcceptor(),
		WebSocketConnectionGateway("localhost:8081", elg),
		60 * 1000,
		1 * 1000
	)
	
	/*elg.acceptorAsScheduledExecutorService.scheduleAtFixedRate({
		println("co: ${server.statistic.connections}, cl: ${server.statistic.clients}")
	}, 500, 500, TimeUnit.MILLISECONDS)
	*/
	Runtime.getRuntime().addShutdownHook(Thread {
		server.stop()
		elg.stop()
	})
}

class ExampleClientAuthorizer : ClientAuthorizer {
	override fun authorizeClient(authorizationKey: InputByteBuffer): Long? {
		return if (authorizationKey.isReadable(8))
			authorizationKey.readLong().takeIf { it != 0L }
		else null
	}
}

class ExampleClientAcceptor : ClientAcceptor {
	override fun acceptClient(client: Client): ClientMessageReceiver {
		return ClientHandler(client)
	}
	
}


class ClientHandler(private val client: Client) : ClientMessageReceiver {
	override fun receiveMessage(message: InputByteBuffer) {
		client.sendMessage(message)
	}
}