package ru.capjack.csi.sandbox.client.common

import ru.capjack.csi.core.Connection
import ru.capjack.csi.core.client.ConnectFailReason
import ru.capjack.csi.core.client.ConnectionAcceptor
import ru.capjack.csi.core.client.ConnectionHandler
import ru.capjack.csi.core.client.ConnectionRecoveryHandler
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.utils.Cancelable
import ru.capjack.tool.utils.concurrency.DelayableAssistant
import kotlin.jvm.Volatile
import kotlin.random.Random

@Suppress("DEPRECATION")
internal class RushUnitImpl(
	override val id: Int,
	override val key: Int,
	private val observer: (unit: RushUnit, event: RushUnit.Event) -> Unit,
	private val assistant: DelayableAssistant

) : RushUnit, ConnectionHandler, ConnectionAcceptor, ConnectionRecoveryHandler {
	
	@Volatile
	override var alive = true
		private set
	
	@Volatile
	private var stopped = false
	
	@Volatile
	private var connection: Connection? = null
	
	@Volatile
	private var spam: Cancelable = Cancelable.DUMMY
	
	init {
		dispatchEvent(RushUnit.Event.CREATE)
	}
	
	
	override fun handleConnectionClose() {
		alive = false
		synchronized(this) {
			spam.cancel()
		}
		dispatchEvent(RushUnit.Event.CLOSE)
	}
	
	override fun handleConnectionCloseTimeout(seconds: Int) {}
	
	override fun handleConnectionLost(): ConnectionRecoveryHandler {
		dispatchEvent(RushUnit.Event.LOST)
		return this
	}
	
	override fun handleConnectionRecovered() {
		dispatchEvent(RushUnit.Event.RECOVER)
	}
	
	override fun handleConnectionMessage(message: InputByteBuffer) {
		dispatchEvent(RushUnit.Event.MESSAGE)
		message.skipRead()
	}
	
	override fun acceptConnection(connection: Connection): ConnectionHandler {
		dispatchEvent(RushUnit.Event.OPEN)
		
		synchronized(this) {
			this.connection = connection
			if (stopped) {
				connection.close()
			}
			else {
				spam()
			}
		}
		
		return this
	}
	
	override fun acceptFail(reason: ConnectFailReason) {
		synchronized(this) {
			alive = false
		}
		dispatchEvent(RushUnit.Event.FAIL(reason))
	}
	
	override fun stop() {
		synchronized(this) {
			stopped = true
			connection?.close()
		}
	}
	
	private fun dispatchEvent(event: RushUnit.Event) {
		observer.invoke(this, event)
	}
	
	private fun scheduleSpam() {
		synchronized(this) {
			spam = assistant.schedule(Random.nextInt(1000, 3000), ::spam)
		}
	}
	
	private fun spam() {
		val message = Random.nextBytes(Random.nextInt(10, 1024))
		message[0] = 0x02
		connection!!.sendMessage(message)
		scheduleSpam()
	}
}


