package ru.capjack.csi.transport.sandbox.server

import ru.capjack.csi.core.server.Client
import ru.capjack.csi.core.server.ClientAcceptor
import ru.capjack.csi.core.server.ClientAuthorizer
import ru.capjack.csi.core.server.ClientHandler
import ru.capjack.csi.core.server.Server
import ru.capjack.csi.transport.server.EventLoopGroups
import ru.capjack.csi.transport.server.WebSocketConnectionGateway
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.logging.Logging
import ru.capjack.tool.utils.concurrency.ScheduledExecutorImpl
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.random.Random

fun main(args: Array<String>) {
	val address = args[0]
	val logger = Logging.getLogger("sandbox")
	
	logger.info("Start executors")
	val elg = EventLoopGroups()
	val outerExecutor: ScheduledExecutorService = Executors.newScheduledThreadPool(4)
	val connectionsExecutor: ScheduledExecutorService = elg.connections//Executors.newScheduledThreadPool(8)
	
	logger.info("Start server on $address")
	val server = Server(
		ScheduledExecutorImpl(connectionsExecutor),
		MyClientAuthorizer(),
		MyClientAcceptor(outerExecutor),
		WebSocketConnectionGateway(elg, address),
		1000 * 5,
		1000 * 5,
		1000 * 5,
		1000 * 5,
		advancedStatistic = true
	)
	
	var stat = server.statistic.snap()
	outerExecutor.scheduleAtFixedRate({
		val s = server.statistic.snap()
		if (s != stat) {
			stat = s
			logger.info(s.toString())
		}
	}, 1000, 1000, TimeUnit.MILLISECONDS)
	
	
	Runtime.getRuntime().addShutdownHook(thread(false, name = "shutdown") {
		
		logger.info("Stop server")
		server.stop()
		
		logger.info("Stop elg")
		elg.stop()
		logger.info("Stop outerExecutor")
		outerExecutor.shutdown()
		outerExecutor.awaitTermination(1, TimeUnit.MINUTES)
		logger.info("Stop connectionsExecutor")
		connectionsExecutor.shutdown()
		connectionsExecutor.awaitTermination(1, TimeUnit.MINUTES)
		
		logger.info("Stopped")
	})
}

class MyClientAuthorizer : ClientAuthorizer {
	override fun authorizeClient(authorizationKey: InputByteBuffer): Long? {
		return if (authorizationKey.isReadable(8))
			authorizationKey.readLong().takeIf { it != 0L }
		else null
	}
}

class MyClientAcceptor(private val outerExecutor: ScheduledExecutorService) : ClientAcceptor {
	override fun acceptClient(client: Client): ClientHandler {
		return MyClientHandler(client, outerExecutor)
	}
}

class MyClientHandler(private val client: Client, outerExecutor: ScheduledExecutorService) : ClientHandler {
	
	private val task = outerExecutor.scheduleWithFixedDelay({
		client.sendMessage(Random.nextBytes(Random.nextInt(1, 20)).apply { set(0, 1) })
	}, Random.nextLong(2000), 2000, TimeUnit.MILLISECONDS)
	
	
	override fun handleMessage(message: InputByteBuffer) {
		client.sendMessage(message)
	}
	
	override fun handleDisconnect() {
		task.cancel(false)
	}
}
