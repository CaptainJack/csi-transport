package ru.capjack.csi.transport.netty.client

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelHandler
import io.netty.channel.EventLoopGroup
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import ru.capjack.csi.core.client.ChannelAcceptor
import ru.capjack.csi.core.client.ChannelGate
import ru.capjack.tool.logging.ownLogger
import java.net.SocketAddress

abstract class NettyChannelGate(
	private val eventLoopGroup: EventLoopGroup,
	private val address: SocketAddress
) : ChannelGate {
	
	override fun openChannel(acceptor: ChannelAcceptor) {
		val bootstrap = Bootstrap()
		bootstrap.group(eventLoopGroup)
		bootstrap.channel(if (eventLoopGroup is EpollEventLoopGroup) EpollSocketChannel::class.java else NioSocketChannel::class.java)
		bootstrap.handler(createChannelInitializer(acceptor))
		
		ownLogger.debug("Connect to $address")
		bootstrap.connect(address)
	}
	
	abstract fun createChannelInitializer(acceptor: ChannelAcceptor): ChannelHandler
}
