package ru.capjack.csi.transport.netty.common

import io.netty.channel.EventLoopGroup
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import java.net.InetSocketAddress
import java.net.SocketAddress

fun factoryEventLoopGroup(threads: Int, tryUseEpoll: Boolean): EventLoopGroup {
	return if (tryUseEpoll && Epoll.isAvailable())
		EpollEventLoopGroup(threads)
	else
		NioEventLoopGroup(threads)
}


fun String.toSocketAddress(): SocketAddress {
	return InetSocketAddress(
		substringBefore(':'),
		substringAfter(':').toInt()
	)
}