package ru.capjack.csi.transport.server

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler
import ru.capjack.csi.core.server.ConnectionAcceptor

internal open class WebSocketChannelInitializer(acceptor: ConnectionAcceptor) : ChannelInitializer<SocketChannel>() {
	
	private val channelAcceptor = WebSocketChannelAcceptor(acceptor)
	
	override fun initChannel(ch: SocketChannel) {
		ch.pipeline().addLast(
			HttpServerCodec(),
			HttpObjectAggregator(65536),
			WebSocketServerCompressionHandler(),
			WebSocketServerProtocolHandler("/", null, true),
			channelAcceptor
		)
	}
}