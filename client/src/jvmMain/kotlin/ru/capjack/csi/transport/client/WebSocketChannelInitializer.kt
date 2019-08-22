package ru.capjack.csi.transport.client

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.DefaultHttpHeaders
import io.netty.handler.codec.http.HttpClientCodec
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler
import io.netty.handler.codec.http.websocketx.WebSocketVersion
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler
import ru.capjack.csi.core.client.ConnectionAcceptor
import java.net.InetSocketAddress
import java.net.URI

internal class WebSocketChannelInitializer(private val acceptor: ConnectionAcceptor, private val address: InetSocketAddress) : ChannelInitializer<SocketChannel>() {
	override fun initChannel(channel: SocketChannel) {
		channel.pipeline().addLast(
			HttpClientCodec(),
			HttpObjectAggregator(65536),
			WebSocketClientCompressionHandler.INSTANCE,
			WebSocketClientProtocolHandler(
				URI("ws", null, address.hostString, address.port, "/", null, null),
				WebSocketVersion.V13,
				null,
				true,
				DefaultHttpHeaders(),
				65536
			),
			WebSocketChannelAcceptor(acceptor)
		)
	}
}
