package ru.capjack.csi.transport.server

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelOption
import ru.capjack.csi.core.server.ConnectionAcceptor
import java.net.InetSocketAddress
import java.net.SocketAddress

class WebSocketConnectionGateway(
	eventLoopGroups: EventLoopGroups,
	address: SocketAddress
) : NettyConnectionGateway(eventLoopGroups, address) {
	
	constructor(eventLoopGroups: EventLoopGroups, address: String) : this(
		eventLoopGroups,
		address.split(':', limit = 2).let { InetSocketAddress(it[0], it[1].toInt()) }
	)
	
	override fun configureOptions(bootstrap: ServerBootstrap) {
		bootstrap.apply {
			option(ChannelOption.SO_BACKLOG, 1024)
			childOption(ChannelOption.TCP_NODELAY, true)
			childOption(ChannelOption.SO_KEEPALIVE, true)
		}
	}
	
	override fun createChannelInitializer(acceptor: ConnectionAcceptor): ChannelHandler {
		return WebSocketChannelInitializer(acceptor)
	}
}
