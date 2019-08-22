package ru.capjack.csi.transport.client

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler
import ru.capjack.csi.core.client.ConnectionAcceptor
import ru.capjack.csi.transport.common.AbstractChannelHandler
import ru.capjack.csi.transport.common.ChannelConnection
import ru.capjack.csi.transport.common.WebSocketChannelHandler
import ru.capjack.csi.transport.common.WebSocketMessageSender

internal class WebSocketChannelAcceptor(
	private val acceptor: ConnectionAcceptor
) : AbstractChannelHandler() {
	
	override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
		when (evt) {
			WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE -> {
				ctx.pipeline()
					.remove(this)
					.addLast(
						WebSocketChannelHandler(
							acceptor.acceptSuccess(
								ChannelConnection(
									ctx.channel(),
									WebSocketMessageSender()
								)
							)
						)
					)
			}
			WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_TIMEOUT  -> {
				ctx.pipeline().remove(this)
				acceptor.acceptFail()
			}
		}
		
		super.userEventTriggered(ctx, evt)
	}
	
	override fun channelUnregistered(ctx: ChannelHandlerContext) {
		acceptor.acceptFail()
	}
}
