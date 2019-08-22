package ru.capjack.tool.csi.transport.sandbox.server

import ru.capjack.tool.csi.core.server.Client
import ru.capjack.tool.csi.core.server.ClientAcceptor
import ru.capjack.tool.csi.core.server.ClientAuthorizer
import ru.capjack.tool.csi.core.server.ClientMessageReceiver
import ru.capjack.tool.csi.core.server.Server
import ru.capjack.tool.csi.core.server.ServerStatistic
import ru.capjack.tool.csi.transport.server.EventLoopGroups
import ru.capjack.tool.csi.transport.server.WebSocketConnectionGateway
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.logging.Logging
import ru.capjack.tool.utils.concurrency.ScheduledExecutorImpl
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

fun main(args: Array<String>) {
	val address = args.getOrElse(0) { "localhost:7777" }
	val logger = Logging.getLogger("sandbox")
	
	logger.info("Start elg")
	val elg = EventLoopGroups()
	
	logger.info("Start server on $address")
	val server = Server(
		ScheduledExecutorImpl(elg.connectionsAsScheduledExecutorService),
		ExampleClientAuthorizer(),
		ExampleClientAcceptor(),
		WebSocketConnectionGateway(address, elg),
		60 * 1000,
		1 * 1000
	)
	
	var statistic: ServerStatistic = server.statistic.snapshot()
	
	elg.acceptorAsScheduledExecutorService.scheduleAtFixedRate({
		val s = server.statistic.snapshot()
		if (s != statistic) {
			statistic = s
			logger.info("connections: ${statistic.connections}, clients: ${statistic.clients}")
		}
	}, 500, 500, TimeUnit.MILLISECONDS)
	
	
	Runtime.getRuntime().addShutdownHook(thread(false, name = "shutdown") {
		
		logger.info("Stop server (connections: ${server.statistic.connections}, clients: ${server.statistic.clients})")
		server.stop()
		
		logger.info("Stop elg  (connections: ${server.statistic.connections}, clients: ${server.statistic.clients})")
		elg.stop()
		
		logger.info("Stopped (connections: ${server.statistic.connections}, clients: ${server.statistic.clients})")
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