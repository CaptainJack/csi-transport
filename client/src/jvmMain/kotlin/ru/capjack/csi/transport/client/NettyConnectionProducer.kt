package ru.capjack.csi.transport.client

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelHandler
import io.netty.channel.EventLoopGroup
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import ru.capjack.csi.core.client.ConnectionAcceptor
import ru.capjack.csi.core.client.ConnectionProducer
import java.net.SocketAddress

abstract class NettyConnectionProducer(
	private val eventLoopGroup: EventLoopGroup,
	private val address: SocketAddress
) : ConnectionProducer {
	
	override fun produceConnection(acceptor: ConnectionAcceptor) {
		val bootstrap = Bootstrap()
		bootstrap.group(eventLoopGroup)
		bootstrap.channel(if (eventLoopGroup is EpollEventLoopGroup) EpollSocketChannel::class.java else NioSocketChannel::class.java)
		bootstrap.handler(createChannelInitializer(acceptor))
		bootstrap.connect(address)
	}
	
	abstract fun createChannelInitializer(acceptor: ConnectionAcceptor): ChannelHandler
}
