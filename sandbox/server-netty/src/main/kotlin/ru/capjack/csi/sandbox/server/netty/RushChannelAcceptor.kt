package ru.capjack.csi.sandbox.server.netty

import ru.capjack.csi.core.Channel
import ru.capjack.csi.core.ChannelHandler
import ru.capjack.csi.core.server.ChannelAcceptor

class RushChannelAcceptor(private val acceptor: ChannelAcceptor, private val statistic: RushStatistic) :
	ChannelAcceptor {
	override fun acceptChannel(channel: Channel): ChannelHandler {
		statistic.add(RushStatistic.Event.CHANNEL_ACCEPT)
		return RushChannelHandler(acceptor.acceptChannel(RushChannel(channel, statistic)), statistic)
	}
	
}