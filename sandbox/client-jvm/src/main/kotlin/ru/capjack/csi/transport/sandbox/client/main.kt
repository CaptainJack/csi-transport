package ru.capjack.csi.transport.sandbox.client

import io.netty.channel.nio.NioEventLoopGroup
import ru.capjack.csi.core.client.Client
import ru.capjack.csi.core.client.ClientAcceptor
import ru.capjack.csi.core.client.ClientConnector
import ru.capjack.csi.core.client.ClientDisconnectReason
import ru.capjack.csi.core.client.ClientHandler
import ru.capjack.csi.core.client.ConnectFailReason
import ru.capjack.csi.core.client.ConnectionRecoveryHandler
import ru.capjack.csi.transport.client.WebSocketConnectionProducer
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.io.getLong
import ru.capjack.tool.io.putLong
import ru.capjack.tool.io.readToArray
import ru.capjack.tool.logging.Logging
import ru.capjack.tool.logging.ownLogger
import ru.capjack.tool.utils.concurrency.ScheduledExecutorImpl
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.thread
import kotlin.random.Random

fun main(args: Array<String>) {
	val logger = Logging.getLogger("sandbox")
	
	val address = args[0]
	val idOffset = args[1].toInt()
	val count = args[2].toInt()
	
	logger.info("Start $count clients with ids at $idOffset on $address")
	
	ForkJoinPool.commonPool()
	
	val elg = NioEventLoopGroup(Runtime.getRuntime().availableProcessors())
	val outerExecutor: ScheduledExecutorService = Executors.newScheduledThreadPool(4)
	val connectionsExecutor: ScheduledExecutorService = elg//Executors.newScheduledThreadPool(8)
	
	val connector = ClientConnector(ScheduledExecutorImpl(connectionsExecutor), WebSocketConnectionProducer(elg, address))
	val counter = CountDownLatch(count)
	val messagingTimes = ConcurrentLinkedQueue<Long>()
	
	val emulators = List(count) {
		ClientEmulator(connector, it + idOffset, idOffset, count, outerExecutor, counter, messagingTimes)
	}
	
	data class Stat(val clients: Int, val mrt: Double)
	
	fun calcStat(): Stat {
		var mrtTime = 0L
		var mrtCount = 0
		val l = count * 3
		while (mrtCount <= l) {
			mrtTime += messagingTimes.poll() ?: break
			++mrtCount
		}
		
		val mrt = mrtTime / mrtCount.coerceAtLeast(1).toDouble()
		
		return Stat(
			emulators.count(ClientEmulator::active),
			mrt
		)
	}
	
	
	var stat: Stat = calcStat()
	val statTask = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
		val s = calcStat()
		if (stat != s) {
			stat = s
			logger.info("clients: ${s.clients}, mrt: ${s.mrt}")
		}
	}, 1, 1, TimeUnit.SECONDS)
	
	
	Runtime.getRuntime().addShutdownHook(thread(false, name = "shutdown") {
		logger.info("Stop emulators")
		emulators.forEach(ClientEmulator::stop)
		
		while (!counter.await(1, TimeUnit.SECONDS)) {
			logger.info("Wait ${counter.count} emulators")
		}
		
		logger.info("Stop elg")
		elg.shutdownGracefully().syncUninterruptibly()
		logger.info("Stop outerExecutor")
		outerExecutor.shutdown()
		outerExecutor.awaitTermination(1, TimeUnit.MINUTES)
		logger.info("Stop connectionsExecutor")
		connectionsExecutor.shutdown()
		connectionsExecutor.awaitTermination(1, TimeUnit.MINUTES)
		
		statTask.cancel(false)
		
		logger.info("Stopped")
	})
	
	
}

class ClientEmulator(
	private val connector: ClientConnector,
	private val id: Int,
	private val idOffset: Int,
	private val idSize: Int,
	val outerExecutor: ScheduledExecutorService,
	private val counter: CountDownLatch,
	private val messagingTimes: ConcurrentLinkedQueue<Long>
) : ClientAcceptor {
	
	val active: Boolean
		get() = clientDelegate != null
	
	@Volatile
	private var running = true
	@Volatile
	private var task: Future<*>? = null
	@Volatile
	private var clientDelegate: ClientDelegate? = null
	
	init {
		task = outerExecutor.submit(::connect)
	}
	
	fun stop() {
		running = false
		task?.cancel(false)
		task = null
		
		val d = clientDelegate
		if (d == null) {
			counter.countDown()
		}
		else {
			clientDelegate = null
			d.stop()
		}
	}
	
	private fun connect() {
		task = null
		if (running) {
			val r = Random.nextInt(100)
			val clientId = when {
				r < 1  -> 0
				r < 10 -> Random.nextInt(idOffset, idOffset + idSize).toLong()
				else   -> id.toLong()
			}
			
			connector.connectClient(ByteArray(8).apply { putLong(0, clientId) }, this)
		}
	}
	
	override fun acceptFail(reason: ConnectFailReason) {
		scheduleConnect()
	}
	
	override fun acceptSuccess(client: Client): ClientHandler {
		val d = ClientDelegate(client, this)
		clientDelegate = d
		if (!running) {
			client.disconnect()
		}
		return d
	}
	
	fun acceptDisconnect() {
		if (!running && clientDelegate == null) {
			counter.countDown()
		}
		else {
			clientDelegate = null
			scheduleConnect()
		}
	}
	
	private fun scheduleConnect() {
		if (running) {
			task = outerExecutor.schedule(::connect, 1, TimeUnit.SECONDS)
		}
	}
	
	fun registerMessagingTime(value: Long) {
		messagingTimes.add(value)
	}
}

class ClientDelegate(private val client: Client, private val emulator: ClientEmulator) : ClientHandler, ConnectionRecoveryHandler {
	
	@Volatile
	private lateinit var task: ScheduledFuture<*>
	
	private val counter = AtomicLong()
	private val outputMessages = ConcurrentLinkedQueue<Pair<Long, ByteArray>>()
	
	init {
		scheduleSendMessage()
	}
	
	private fun scheduleSendMessage() {
		task = emulator.outerExecutor.schedule(::sendMessage, 1000, TimeUnit.MILLISECONDS) // Random.nextLong(300, 5000),
	}
	
	private fun sendMessage() {
		val id = counter.incrementAndGet()
		val message = Random.nextBytes(Random.nextInt(10, 500))
		message[0] = 0
		message.putLong(1, id)
		
		val time = System.currentTimeMillis()
		outputMessages.add(time to message)
		client.sendMessage(message)
		
		scheduleSendMessage()
	}
	
	override fun handleDisconnect(reason: ClientDisconnectReason) {
		task.cancel(false)
		emulator.acceptDisconnect()
	}
	
	override fun handleMessage(message: InputByteBuffer) {
		val time = System.currentTimeMillis()
		
		val inputMessage = message.readToArray()
		
		
		if (inputMessage[0] == 0.toByte()) {
			val inputMessageId = inputMessage.getLong(1)
			
			val output = outputMessages.poll()
			
			if (output == null || !output.second.contentEquals(inputMessage)) {
				val outputMessageId = output?.second?.getLong(1)
				ownLogger.error("Input message not equals output ($inputMessageId, $outputMessageId)")
				println("!!!")
				stop()
			}
			else {
				emulator.registerMessagingTime(time - output.first)
			}
		}
	}
	
	override fun handleServerShutdownTimeout(millis: Int) {
		emulator.outerExecutor.schedule(::stop, Random.nextLong(10, millis + 1000L), TimeUnit.MILLISECONDS)
	}
	
	override fun handleConnectionLost(): ConnectionRecoveryHandler {
		return this
	}
	
	override fun handleConnectionRecovered() {
	}
	
	fun stop() {
		client.disconnect()
	}
}
