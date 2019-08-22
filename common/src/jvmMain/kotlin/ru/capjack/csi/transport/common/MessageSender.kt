package ru.capjack.csi.transport.common

import io.netty.buffer.ByteBuf
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture

interface MessageSender {
	fun sendMessage(channel: Channel, message: ByteBuf): ChannelFuture
}
