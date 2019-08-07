package ru.capjack.tool.csi.transport.server

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler
import ru.capjack.tool.csi.core.server.ConnectionAcceptor

@Sharable
internal class WebSocketChannelAcceptor(
	private val acceptor: ConnectionAcceptor
) : ChannelInboundHandlerAdapter() {
	
	
	override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
		super.userEventTriggered(ctx, evt)
		if (evt is WebSocketServerProtocolHandler.HandshakeComplete) {
			ctx.pipeline()
				.remove(this)
				.addLast(CsiChannelHandler(acceptor, ctx.channel()))
		}
	}
}
