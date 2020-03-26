package ru.capjack.csi.sandbox.server.netty

import java.util.concurrent.atomic.AtomicLong

class RushStatistic {
	
	enum class Event {
		CHANNEL_ACCEPT,
		CHANNEL_CLOSE_OUTPUT,
		CHANNEL_CLOSE_INPUT,
		CHANNEL_DATA_OUTPUT,
		CHANNEL_DATA_INPUT,
		CONNECTION_ACCEPT,
		CONNECTION_CLOSE_INPUT,
		CONNECTION_CLOSE_OUTPUT
	}
	
	private var attributes = enumValues<Event>().associate { it to AtomicLong() }
	
	fun add(event: Event) {
		attributes.getValue(event).getAndIncrement()
	}
	
	fun add(event: Event, value: Int) {
		attributes.getValue(event).getAndAdd(value.toLong())
	}
	
	operator fun get(event: Event): Long {
		return attributes.getValue(event).get()
	}
	
	fun pull(event: Event): Long {
		return attributes.getValue(event).getAndSet(0)
	}
}