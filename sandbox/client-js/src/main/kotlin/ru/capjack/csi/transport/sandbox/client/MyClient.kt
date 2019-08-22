package ru.capjack.csi.transport.sandbox.client

import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLTableCellElement
import org.w3c.dom.HTMLTableRowElement
import org.w3c.dom.HTMLTableSectionElement
import ru.capjack.csi.core.client.Client
import ru.capjack.csi.core.client.ClientAcceptor
import ru.capjack.csi.core.client.ClientDisconnectReason
import ru.capjack.csi.core.client.ClientHandler
import ru.capjack.csi.core.client.ConnectFailReason
import ru.capjack.csi.core.client.ConnectionRecoveryHandler
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.io.readToArray
import ru.capjack.tool.utils.Cancelable
import ru.capjack.tool.utils.collections.ArrayQueue
import ru.capjack.tool.utils.concurrency.ScheduledExecutor
import kotlin.browser.window
import kotlin.math.roundToInt
import kotlin.random.Random

class MyClient(
	private val number: Int,
	private val executor: ScheduledExecutor,
	tBody: HTMLTableSectionElement
) : ClientAcceptor, ClientHandler, ConnectionRecoveryHandler {
	
	private var client: Client? = null
	
	private lateinit var state: HTMLElement
	private lateinit var messaging: HTMLElement
	private lateinit var disconnect: HTMLButtonElement
	
	private val outputMessages = ArrayQueue<ByteArray>()
	private val sendMessageFn = ::scheduleSendMessage
	private var messenger: Cancelable = Cancelable.DUMMY
	private var outputMessageTime: Double = 0.0
	
	init {
		tBody.appendElement<HTMLTableRowElement>("tr") {
			appendElement<HTMLTableCellElement>("th") {
				scope = "row"
				innerText = number.toString()
			}
			
			appendElement<HTMLTableCellElement>("td") {
				state = appendElement("span") {
					className = "bg-light text-dark"
					innerText = "Connecting"
				}
			}
			
			appendElement<HTMLTableCellElement>("td") {
				align="right"
				messaging = appendElement("code")
			}
			
			appendElement<HTMLTableCellElement>("td") {
				align="right"
				disconnect = appendElement("button") {
					className = "btn btn-outline-danger btn-sm p-0"
					style.border = "0"
					innerHTML = "<small>Disconnect</small>"
					disabled = true
					onclick = { disconnect() }
				}
			}
		}
	}
	
	fun disconnect() {
		disconnect.disabled = true
		client?.disconnect()
	}
	
	private fun scheduleSendMessage() {
		val message = Random.nextBytes(Random.nextInt(1, 600))
		outputMessages.add(message)
		
		client!!.sendMessage(message)
		
		messenger = executor.schedule(1000, sendMessageFn)
		
		outputMessageTime = window.performance.now()
	}
	
	override fun handleMessage(message: InputByteBuffer) {
		val ms = (window.performance.now() - outputMessageTime).roundToInt()
		
		messaging.innerText = ms.toString()
		
		val inputMessage = message.readToArray()
		val outputMessage = outputMessages.poll()
		if (outputMessage == null || !outputMessage.contentEquals(inputMessage)) {
			console.error("[$number] Input message not equals output", inputMessage, outputMessage)
			messaging.className = "bg-danger text-white"
		}
	}
	
	override fun acceptSuccess(client: Client): ClientHandler {
		state.innerText = "Connected"
		state.className = "bg-success text-white"
		
		this.client = client
		
		scheduleSendMessage()
		
		disconnect.disabled = false
		
		return this
	}
	
	override fun acceptFail(reason: ConnectFailReason) {
		state.innerText = reason.name
		state.className = "bg-danger text-white"
	}
	
	override fun handleDisconnect(reason: ClientDisconnectReason) {
		disconnect.disabled = true
		
		state.innerText = reason.name
		state.className = "bg-secondary text-white"
		client = null
		messenger.cancel()
	}
	
	override fun handleServerShutdownTimeout(millis: Int) {
		state.innerText = "Shutdown: ${millis} ms"
		state.className = "bg-info text-white"
	}
	
	override fun handleConnectionLost(): ConnectionRecoveryHandler {
		state.innerText = "Lost"
		state.className = "bg-warning text-dark"
		return this
	}
	
	override fun handleConnectionRecovered() {
		state.innerText = "Connected"
		state.className = "bg-success text-white"
	}
}