package ru.capjack.csi.transport.netty.common

import io.netty.buffer.ByteBuf
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture

interface ChannelWriter {
	fun writeAndFlush(channel: Channel, message: ByteBuf): ChannelFuture
}
