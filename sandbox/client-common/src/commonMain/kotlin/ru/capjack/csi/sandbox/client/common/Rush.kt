package ru.capjack.csi.sandbox.client.common

import ru.capjack.csi.core.client.ChannelGate
import ru.capjack.csi.core.client.Client
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.io.putInt
import ru.capjack.tool.utils.Stoppable
import ru.capjack.tool.utils.concurrency.DelayableAssistant
import ru.capjack.tool.utils.concurrency.ObjectPool
import kotlin.jvm.Volatile

@Suppress("DEPRECATION")
class Rush(
	version: Int,
	activityTimeoutSeconds: Int,
	private val assistant: DelayableAssistant,
	byteBuffers: ObjectPool<ByteBuffer>,
	gate: ChannelGate,
	private val unitObserver: (unit: RushUnit, event: RushUnit.Event) -> Unit
) : Stoppable {
	
	val active
		get() = synchronized(units) { units.any(RushUnit::alive) }
	
	val activeUnits
		get() = synchronized(units) { units.count(RushUnit::alive) }
	
	@Volatile
	private var running = true
	
	private val client = Client(assistant, byteBuffers, gate, version, activityTimeoutSeconds)
	private val units = mutableListOf<RushUnitImpl>()
	
	fun produceUnit(key: Int): RushUnit {
		synchronized(units) {
			val id = units.size + 1
			val unit = RushUnitImpl(id, key, unitObserver, assistant)
			
			if (running) {
				units.add(unit)
				client.connect(ByteArray(4).apply { putInt(0, key) }, unit)
			}
			return unit
		}
	}
	
	override fun stop() {
		synchronized(units) {
			running = false
			units.forEach(RushUnitImpl::stop)
		}
	}
}