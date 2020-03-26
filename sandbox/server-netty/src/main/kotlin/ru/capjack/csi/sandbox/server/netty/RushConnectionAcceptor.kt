package ru.capjack.csi.sandbox.server.netty

import ru.capjack.csi.core.Connection
import ru.capjack.csi.core.server.ConnectionAcceptor
import ru.capjack.csi.core.server.ConnectionHandler
import ru.capjack.tool.utils.concurrency.DelayableAssistant

class RushConnectionAcceptor(
	private val assistant: DelayableAssistant,
	private val statistic: RushStatistic
) : ConnectionAcceptor<Int> {
	override fun acceptConnection(identity: Int, connection: Connection): ConnectionHandler {
		statistic.add(RushStatistic.Event.CONNECTION_ACCEPT)
		return RushConnectionHandler(assistant, connection, statistic)
	}
}

