package ru.capjack.csi.transport.sandbox.client

import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLTableSectionElement
import ru.capjack.csi.core.client.ClientConnector
import ru.capjack.csi.transport.client.WebSocketConnectionProducer
import ru.capjack.tool.utils.LoggingErrorCatcher
import ru.capjack.tool.utils.concurrency.ScheduledExecutor
import kotlin.browser.document
import kotlin.random.Random

class Clients(
	errorCatcher: LoggingErrorCatcher,
	private val executor: ScheduledExecutor,
	val tBody: HTMLTableSectionElement
) {
	private val list = mutableListOf<MyClient>()
	
	private val connector by lazy {
		val address = document.get<HTMLInputElement>("address")
		address.disabled = true
		
		ClientConnector(
			executor,
			WebSocketConnectionProducer(errorCatcher, address.value, false)
		)
	}
	
	fun add(count: Int) {
		repeat(count) {
			val client = MyClient(list.size + 1, executor, tBody)
			list.add(client)
			connector.connectClient(
				Random.nextBytes(8),
				client
			)
		}
	}
	
	fun disconnect() {
		list.forEach { it.disconnect() }
	}
}