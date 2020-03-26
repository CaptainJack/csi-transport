package ru.capjack.csi.transport.netty.server

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelHandler
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import ru.capjack.csi.core.server.ChannelAcceptor
import ru.capjack.csi.core.server.ChannelGate
import ru.capjack.tool.logging.ownLogger
import ru.capjack.tool.utils.Closeable
import java.net.SocketAddress

abstract class NettyChannelGate(
	private val eventLoopGroups: ServerEventLoopGroups,
	private val address: SocketAddress
) : ChannelGate {
	
	override fun openGate(acceptor: ChannelAcceptor): Closeable {
		try {
			val bootstrap = ServerBootstrap()
			bootstrap.group(eventLoopGroups.acceptorGroup, eventLoopGroups.channelsGroup)
			bootstrap.channel(if (eventLoopGroups.channelsGroup is EpollEventLoopGroup) EpollServerSocketChannel::class.java else NioServerSocketChannel::class.java)
			
			configureOptions(bootstrap)
			
			bootstrap.childHandler(createChannelInitializer(acceptor))
			
			ownLogger.debug("Bind to $address")
			
			val future = bootstrap.bind(address).syncUninterruptibly()
			
			if (!future.isSuccess) {
				throw RuntimeException("Fail on bind to $address")
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
	
	protected abstract fun createChannelInitializer(acceptor: ChannelAcceptor): ChannelHandler
}
