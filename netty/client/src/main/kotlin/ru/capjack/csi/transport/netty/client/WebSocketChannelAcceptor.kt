package ru.capjack.csi.transport.netty.client

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler
import ru.capjack.csi.core.client.ChannelAcceptor
import ru.capjack.csi.transport.netty.common.ChannelDelegate
import ru.capjack.csi.transport.netty.common.NettyChannelHandler
import ru.capjack.csi.transport.netty.common.WebSocketChannelHandler
import ru.capjack.csi.transport.netty.common.WebSocketChannelWriter

internal class WebSocketChannelAcceptor(
	private val acceptor: ChannelAcceptor
) : NettyChannelHandler() {
	
	override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
		when (evt) {
			WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE -> {
				val channel = ChannelDelegate(ctx.channel(), WebSocketChannelWriter())
				val handler = acceptor.acceptChannel(channel)
				ctx.pipeline()
					.remove(this)
					.addLast(WebSocketChannelHandler(handler))
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
