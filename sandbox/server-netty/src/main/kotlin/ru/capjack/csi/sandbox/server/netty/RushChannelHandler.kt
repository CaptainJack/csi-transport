package ru.capjack.csi.sandbox.server.netty

import ru.capjack.csi.core.ChannelHandler
import ru.capjack.tool.io.InputByteBuffer

class RushChannelHandler(private val handler: ChannelHandler, private val statistic: RushStatistic) :
	ChannelHandler {
	override fun handleChannelClose() {
		statistic.add(RushStatistic.Event.CHANNEL_CLOSE_INPUT)
		handler.handleChannelClose()
	}
	
	override fun handleChannelInput(data: InputByteBuffer) {
		statistic.add(RushStatistic.Event.CHANNEL_DATA_INPUT, data.readableSize)
		handler.handleChannelInput(data)
	}
}