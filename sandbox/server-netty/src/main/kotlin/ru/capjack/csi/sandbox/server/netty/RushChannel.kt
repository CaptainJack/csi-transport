package ru.capjack.csi.sandbox.server.netty

import ru.capjack.csi.core.Channel
import ru.capjack.tool.io.InputByteBuffer

class RushChannel(private val channel: Channel, private val statistic: RushStatistic) :
	Channel {
	override val id: Any
		get() = channel.id
	
	override fun close() {
		statistic.add(RushStatistic.Event.CHANNEL_CLOSE_OUTPUT)
		channel.close()
	}
	
	override fun send(data: Byte) {
		statistic.add(RushStatistic.Event.CHANNEL_DATA_OUTPUT, 1)
		channel.send(data)
	}
	
	override fun send(data: ByteArray) {
		statistic.add(RushStatistic.Event.CHANNEL_DATA_OUTPUT, data.size)
		channel.send(data)
	}
	
	override fun send(data: InputByteBuffer) {
		statistic.add(RushStatistic.Event.CHANNEL_DATA_OUTPUT, data.readableSize)
		channel.send(data)
	}
}