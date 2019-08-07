package ru.capjack.tool.csi.transport.server

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelOption
import ru.capjack.tool.csi.core.server.ConnectionAcceptor
import ru.capjack.tool.logging.ownLogger
import ru.capjack.tool.utils.Closeable
import java.net.InetSocketAddress
import java.net.SocketAddress

class WebSocketConnectionGateway(
	private val address: String,
	eventLoopGroups: EventLoopGroups
) : NettyConnectionGateway(eventLoopGroups) {
	
	override fun open(acceptor: ConnectionAcceptor): Closeable {
		ownLogger.info("Gateway opening on $address")
		return super.open(acceptor)
	}
	
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
	
	override fun getSocketAddress(): SocketAddress {
		return address
			.split(':', limit = 2)
			.let { InetSocketAddress(it[0], it[1].toInt()) }
	}
}
