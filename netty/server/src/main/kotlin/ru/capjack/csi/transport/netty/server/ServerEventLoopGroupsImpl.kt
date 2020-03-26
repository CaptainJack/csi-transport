package ru.capjack.csi.transport.netty.server

import ru.capjack.csi.transport.netty.common.factoryEventLoopGroup

class ServerEventLoopGroupsImpl(
	acceptorThreads: Int = 2,
	connectionsThreads: Int = Runtime.getRuntime().availableProcessors(),
	tryUseEpoll: Boolean = true
) : ServerEventLoopGroups {
	
	override val acceptorGroup = factoryEventLoopGroup(acceptorThreads, tryUseEpoll)
	override val channelsGroup = factoryEventLoopGroup(connectionsThreads, tryUseEpoll)
	
	override fun stop() {
		acceptorGroup.shutdownGracefully().syncUninterruptibly()
		channelsGroup.shutdownGracefully().syncUninterruptibly()
	}
}

