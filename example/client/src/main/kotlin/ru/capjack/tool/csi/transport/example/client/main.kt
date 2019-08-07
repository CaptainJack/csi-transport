package ru.capjack.tool.csi.transport.example.client

import org.w3c.dom.HTMLButtonElement
import ru.capjack.tool.csi.core.client.Client
import ru.capjack.tool.csi.core.client.ClientAcceptor
import ru.capjack.tool.csi.core.client.ClientConnector
import ru.capjack.tool.csi.core.client.ClientDisconnectReason
import ru.capjack.tool.csi.core.client.ClientHandler
import ru.capjack.tool.csi.core.client.ConnectFailReason
import ru.capjack.tool.csi.core.client.ConnectionRecoveryHandler
import ru.capjack.tool.csi.transport.client.WebSocketConnectionProducer
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.io.putLong
import ru.capjack.tool.logging.Level
import ru.capjack.tool.logging.Logging
import ru.capjack.tool.utils.LoggingErrorCatcher
import ru.capjack.tool.utils.concurrency.ScheduledExecutor
import ru.capjack.tool.utils.concurrency.WgsScheduledExecutor
import kotlin.browser.document
import kotlin.browser.window
import kotlin.random.Random

fun main() {
	Logging.setLevel(Level.TRACE)
	
	val errorCatcher = LoggingErrorCatcher()
	val executor = WgsScheduledExecutor(window)
	
	val connector = ClientConnector(
		executor,
		WebSocketConnectionProducer(errorCatcher, "localhost:8081", false)
	)
	
	val clientAcceptor = ExampleClientAcceptor(executor)
	
	document.getElementById("runButton").unsafeCast<HTMLButtonElement>().onclick = {
		repeat(1) {
			executor.execute {
				val id = Random.nextLong()
				connector.connectClient(ByteArray(8).apply { putLong(0, id) }, clientAcceptor)
			}
		}
	}
}


class ExampleClientAcceptor(private val executor: ScheduledExecutor) : ClientAcceptor {
	override fun acceptSuccess(client: Client): ClientHandler {
		console.log("Accept Success")
		return ExampleClientHandler(executor, client)
	}
	
	override fun acceptFail(reason: ConnectFailReason) {
		console.log("Accept Fail")
	}
	
}

class ExampleClientHandler(
	executor: ScheduledExecutor,
	private val client: Client
) : ClientHandler, ConnectionRecoveryHandler {
	
	private val messenger = executor.schedule(5 * 1000) {
		client.sendMessage(Random.nextBytes(1024 * 64 + 100))
	}
	
	override fun handleConnectionLost(): ConnectionRecoveryHandler {
		console.log("Connection Lost")
		return this
	}
	
	override fun handleConnectionRecovered() {
		console.log("Connection Recovered")
	}
	
	override fun handleDisconnect(reason: ClientDisconnectReason) {
		console.log("Disconnect by $reason")
		messenger.cancel()
	}
	
	override fun handleMessage(message: InputByteBuffer) {
//		console.log("Message")
		message.readSkip()
	}
	
	override fun handleServerShutdownTimeout(millis: Int) {
		console.log("ServerShutdownTimeout $millis")
	}
}
