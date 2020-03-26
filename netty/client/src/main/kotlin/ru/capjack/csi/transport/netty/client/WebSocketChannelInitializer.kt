package ru.capjack.csi.transport.netty.client

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.DefaultHttpHeaders
import io.netty.handler.codec.http.HttpClientCodec
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler
import io.netty.handler.codec.http.websocketx.WebSocketVersion
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler
import ru.capjack.csi.core.client.ChannelAcceptor
import java.net.URI

internal class WebSocketChannelInitializer(private val acceptor: ChannelAcceptor, private val uri: URI) : ChannelInitializer<SocketChannel>() {
	override fun initChannel(channel: SocketChannel) {
		channel.pipeline().addLast(
			HttpClientCodec(),
			HttpObjectAggregator(65536),
			WebSocketClientCompressionHandler.INSTANCE,
			WebSocketClientProtocolHandler(
				uri,
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
