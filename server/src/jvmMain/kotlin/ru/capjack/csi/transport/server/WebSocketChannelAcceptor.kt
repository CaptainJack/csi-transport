package ru.capjack.csi.transport.server

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler
import ru.capjack.csi.core.server.ConnectionAcceptor
import ru.capjack.csi.transport.common.AbstractChannelHandler
import ru.capjack.csi.transport.common.ChannelConnection
import ru.capjack.csi.transport.common.WebSocketChannelHandler
import ru.capjack.csi.transport.common.WebSocketMessageSender

@Sharable
internal class WebSocketChannelAcceptor(
	private val acceptor: ConnectionAcceptor
) : AbstractChannelHandler() {
	
	private val sender = WebSocketMessageSender()
	
	override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
		if (evt is WebSocketServerProtocolHandler.HandshakeComplete) {
			ctx.pipeline()
				.remove(this)
				.addLast(
					WebSocketChannelHandler(
						acceptor.acceptConnection(
							ChannelConnection(
								ctx.channel(),
								sender
							)
						)
					)
				)
		}
		
		super.userEventTriggered(ctx, evt)
	}
	
}
