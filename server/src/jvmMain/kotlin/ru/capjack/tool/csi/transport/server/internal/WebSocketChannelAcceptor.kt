package ru.capjack.tool.csi.transport.server.internal

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler
import ru.capjack.tool.csi.core.server.ConnectionAcceptor
import ru.capjack.tool.logging.ownLogger
import java.io.IOException

@Sharable
internal class WebSocketChannelAcceptor(
	private val acceptor: ConnectionAcceptor
) : ChannelInboundHandlerAdapter() {
	
	override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
		if (evt is WebSocketServerProtocolHandler.HandshakeComplete) {
			ctx.pipeline()
				.remove(this)
				.addLast(WebSocketChannelHandler(acceptor, ctx.channel()))
		}
		else {
			super.userEventTriggered(ctx, evt)
		}
	}
	
	
	override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
		if (cause !is IOException || cause.message != "Connection reset by peer") {
			ownLogger.warn("Uncaught exception", cause)
		}
		ctx.close()
	}
}
