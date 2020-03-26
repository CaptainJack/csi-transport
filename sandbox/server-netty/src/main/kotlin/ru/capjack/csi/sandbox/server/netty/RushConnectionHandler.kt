package ru.capjack.csi.sandbox.server.netty

import ru.capjack.csi.core.Connection
import ru.capjack.csi.core.server.ConnectionHandler
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.utils.Cancelable
import ru.capjack.tool.utils.concurrency.DelayableAssistant
import kotlin.random.Random

class RushConnectionHandler(private val assistant: DelayableAssistant, private val connection: Connection, private val statistic: RushStatistic) : ConnectionHandler {
	@Volatile
	private var spam: Cancelable = Cancelable.DUMMY
	
	init {
		spam()
		if (Random.nextBoolean()) {
			connection.close()
		}
	}
	
	override fun handleConnectionMessage(message: InputByteBuffer) {
		when (message.readByte().toInt()) {
			0x02 -> connection.sendMessage(message)
			0x03 -> connection.close()
			0x04 -> throw RuntimeException()
		}
	}
	
	override fun handleConnectionClose() {
		statistic.add(RushStatistic.Event.CONNECTION_CLOSE_INPUT)
		synchronized(this) {
			spam.cancel()
		}
	}
	
	private fun scheduleSpam() {
		synchronized(this) {
			spam = assistant.schedule(Random.nextInt(1000, 3000), ::spam)
		}
	}
	
	private fun spam() {
		val message = Random.nextBytes(Random.nextInt(1, 1024))
		connection.sendMessage(message)
		scheduleSpam()
	}
}