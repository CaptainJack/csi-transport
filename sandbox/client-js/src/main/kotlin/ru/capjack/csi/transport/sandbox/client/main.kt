package ru.capjack.csi.transport.sandbox.client

import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLTableSectionElement
import ru.capjack.tool.logging.Level
import ru.capjack.tool.logging.Logging
import ru.capjack.tool.utils.InstantTime
import ru.capjack.tool.utils.LoggingErrorCatcher
import ru.capjack.tool.utils.concurrency.ScheduledExecutor
import ru.capjack.tool.utils.concurrency.WgsScheduledExecutor
import kotlin.browser.document
import kotlin.browser.window

fun main() {
	Logging.setLevel(Level.INFO)
	
	val errorCatcher = LoggingErrorCatcher()
	val time = window.performance.unsafeCast<InstantTime>()
	val executor: ScheduledExecutor = WgsScheduledExecutor(
		LoggingErrorCatcher(),
		time,
		window
	)
	
	val clients = Clients(errorCatcher, executor, document.get("clients"))
	
	document.get<HTMLButtonElement>("clients-add").onclick = {
		clients.add(document.get<HTMLInputElement>("clients-add-count").value.toInt())
	}
	
	document.getElementById("disconnect-all").unsafeCast<HTMLButtonElement>().onclick = {
		clients.disconnect()
	}
}
