package ru.capjack.tool.csi.transport.server

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelHandler
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import ru.capjack.tool.csi.core.server.ConnectionAcceptor
import ru.capjack.tool.csi.core.server.ConnectionGateway
import ru.capjack.tool.logging.ownLogger
import ru.capjack.tool.utils.Closeable
import java.net.SocketAddress

abstract class NettyConnectionGateway(
	private val eventLoopGroups: EventLoopGroups
) : ConnectionGateway {
	override fun open(acceptor: ConnectionAcceptor): Closeable {
		try {
			val bootstrap = ServerBootstrap()
			bootstrap.group(eventLoopGroups.acceptor, eventLoopGroups.connections)
			bootstrap.channel(if (eventLoopGroups.connections is EpollEventLoopGroup) EpollServerSocketChannel::class.java else NioServerSocketChannel::class.java)
			
			configureOptions(bootstrap)
			
			bootstrap.childHandler(createChannelInitializer(acceptor))
			
			val socketAddress = getSocketAddress()
			val future = bootstrap.bind(socketAddress)
			
			future.syncUninterruptibly()
			
			if (!future.isSuccess) {
				throw RuntimeException("Fail on bind to $socketAddress")
			}
			
			val channel = future.channel()
			return Closeable {
				channel.close().syncUninterruptibly()
			}
		}
		catch (e: Throwable) {
			throw RuntimeException("Gateway opening failed", e)
		}
	}
	
	protected abstract fun configureOptions(bootstrap: ServerBootstrap)
	
	protected abstract fun createChannelInitializer(acceptor: ConnectionAcceptor): ChannelHandler
	
	protected abstract fun getSocketAddress(): SocketAddress
}
