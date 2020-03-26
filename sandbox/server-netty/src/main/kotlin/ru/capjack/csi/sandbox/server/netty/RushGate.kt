package ru.capjack.csi.sandbox.server.netty

import ru.capjack.csi.core.server.ChannelAcceptor
import ru.capjack.csi.core.server.ChannelGate
import ru.capjack.tool.utils.Closeable

class RushGate(private val gate: ChannelGate, private val statistic: RushStatistic) :
	ChannelGate {
	override fun openGate(acceptor: ChannelAcceptor): Closeable {
		return gate.openGate(RushChannelAcceptor(acceptor, statistic))
	}
	
}