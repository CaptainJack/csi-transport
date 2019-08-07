package ru.capjack.tool.csi.transport.server

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler
import ru.capjack.tool.csi.core.server.ConnectionAcceptor

internal open class WebSocketChannelInitializer(acceptor: ConnectionAcceptor) : ChannelInitializer<SocketChannel>() {
	
	private val channelAcceptor = WebSocketChannelAcceptor(acceptor)
	
	override fun initChannel(ch: SocketChannel) {
		ch.pipeline().apply {
			addLast(HttpServerCodec())
			addLast(HttpObjectAggregator(65536))
			addLast(WebSocketServerCompressionHandler())
			addLast(WebSocketServerProtocolHandler("/", null, true))
			addLast(channelAcceptor)
		}
	}
}
