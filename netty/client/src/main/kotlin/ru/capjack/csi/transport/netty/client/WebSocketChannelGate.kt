package ru.capjack.csi.transport.netty.client

import io.netty.channel.ChannelHandler
import io.netty.channel.EventLoopGroup
import ru.capjack.csi.core.client.ChannelAcceptor
import java.net.InetSocketAddress
import java.net.URI

class WebSocketChannelGate(
	eventLoopGroup: EventLoopGroup,
	private val uri: URI
) : NettyChannelGate(eventLoopGroup, InetSocketAddress(uri.host, uri.port)) {
	
	override fun createChannelInitializer(acceptor: ChannelAcceptor): ChannelHandler {
		return WebSocketChannelInitializer(acceptor, uri)
	}
}
