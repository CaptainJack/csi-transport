package ru.capjack.csi.sandbox.server.netty

import ru.capjack.csi.core.Connection
import ru.capjack.tool.io.InputByteBuffer

class RushConnectionConnection(private val connection: Connection, private val statistic: RushStatistic) :
	Connection {
	override val id: Long
		get() = connection.id
	
	override fun close() {
		statistic.add(RushStatistic.Event.CONNECTION_CLOSE_OUTPUT)
		connection.close()
	}
	
	override fun close(handler: () -> Unit) {
		statistic.add(RushStatistic.Event.CONNECTION_CLOSE_OUTPUT)
		connection.close(handler)
	}
	
	override fun sendMessage(data: Byte) {
		connection.sendMessage(data)
	}
	
	override fun sendMessage(data: ByteArray) {
		connection.sendMessage(data)
	}
	
	override fun sendMessage(data: InputByteBuffer) {
		connection.sendMessage(data)
	}
	
}