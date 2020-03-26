package ru.capjack.csi.transport.netty.server

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelOption
import ru.capjack.csi.core.server.ChannelAcceptor
import ru.capjack.csi.transport.netty.common.toSocketAddress
import java.net.SocketAddress

class WebSocketChannelGate(
	eventLoopGroups: ServerEventLoopGroups,
	address: SocketAddress
) : NettyChannelGate(eventLoopGroups, address) {
	
	constructor(eventLoopGroups: ServerEventLoopGroups, address: String) : this(eventLoopGroups, address.toSocketAddress())
	
	override fun configureOptions(bootstrap: ServerBootstrap) {
		bootstrap.apply {
			option(ChannelOption.SO_BACKLOG, 1024)
			childOption(ChannelOption.TCP_NODELAY, true)
			childOption(ChannelOption.SO_KEEPALIVE, true)
		}
	}
	
	override fun createChannelInitializer(acceptor: ChannelAcceptor): ChannelHandler {
		return WebSocketChannelInitializer(acceptor)
	}
}
