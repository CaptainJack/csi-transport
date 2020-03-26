package ru.capjack.csi.transport.netty.common

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import ru.capjack.tool.logging.ownLogger
import java.io.IOException

abstract class NettyChannelHandler() : ChannelInboundHandlerAdapter() {
	override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
		if (cause !is IOException) {
			ownLogger.warn("Uncaught exception", cause)
		}
		ctx.close()
	}
}