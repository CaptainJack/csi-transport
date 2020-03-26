package ru.capjack.csi.transport.netty.server

import io.netty.channel.EventLoopGroup
import ru.capjack.tool.utils.Stoppable

interface ServerEventLoopGroups : Stoppable {
	val acceptorGroup: EventLoopGroup
	val channelsGroup: EventLoopGroup
}
