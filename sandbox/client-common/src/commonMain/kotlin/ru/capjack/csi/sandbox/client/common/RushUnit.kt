package ru.capjack.csi.sandbox.client.common

import ru.capjack.csi.core.client.ConnectFailReason
import ru.capjack.tool.utils.Stoppable

interface RushUnit : Stoppable {
	val id: Int
	val key: Int
	val alive: Boolean
	
	sealed class Event {
		object CREATE : Event()
		object OPEN : Event()
		object CLOSE : Event()
		object LOST : Event()
		object RECOVER : Event()
		object MESSAGE : Event()
		class FAIL(val reason: ConnectFailReason) : Event()
	}
}