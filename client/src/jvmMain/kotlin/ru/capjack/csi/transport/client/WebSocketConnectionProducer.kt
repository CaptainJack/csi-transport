package ru.capjack.csi.transport.client

import io.netty.channel.ChannelHandler
import io.netty.channel.EventLoopGroup
import ru.capjack.csi.core.client.ConnectionAcceptor
import java.net.InetSocketAddress

class WebSocketConnectionProducer(
	eventLoopGroup: EventLoopGroup,
	private val address: InetSocketAddress
) : NettyConnectionProducer(eventLoopGroup, address) {
	
	constructor(eventLoopGroup: EventLoopGroup, address: String) : this(
		eventLoopGroup,
		address.split(':', limit = 2).let { InetSocketAddress(it[0], it[1].toInt()) }
	)
	
	override fun createChannelInitializer(acceptor: ConnectionAcceptor): ChannelHandler {
		return WebSocketChannelInitializer(acceptor, address)
	}
}
