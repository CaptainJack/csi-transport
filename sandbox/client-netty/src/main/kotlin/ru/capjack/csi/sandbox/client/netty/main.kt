package ru.capjack.csi.sandbox.client.netty

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import ru.capjack.csi.core.client.ConnectFailReason
import ru.capjack.csi.sandbox.client.common.Rush
import ru.capjack.csi.sandbox.client.common.RushUnit
import ru.capjack.csi.transport.netty.client.WebSocketChannelGate
import ru.capjack.csi.transport.netty.common.factoryEventLoopGroup
import ru.capjack.tool.logging.Logging
import ru.capjack.tool.utils.concurrency.ArrayObjectPool
import ru.capjack.tool.utils.concurrency.ExecutorDelayableAssistant
import java.net.URI
import java.time.Clock
import java.time.LocalTime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.thread
import kotlin.random.Random

private class Args(parser: ArgParser) {
	val address by parser.positional("Address")
	val count by parser.storing("Count") { toInt() }.default(100)
	val version by parser.storing("Version") { toInt() }.default(0)
	val activity by parser.storing("Activity timeout seconds") { toInt() }.default(10)
}

fun main(args: Array<String>) {
	val logger = Logging.getLogger("sandbox")
	
	val a = ArgParser(args).parseInto(::Args)
	
	logger.info("Start ${a.count} clients on ${a.address}")
	
	val assistantExecutor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors())
	val elg = factoryEventLoopGroup(Runtime.getRuntime().availableProcessors(), true)
	val statistic = RushStatistic()
	
	val assistant = ExecutorDelayableAssistant(assistantExecutor)
	val byteBufferAllocator = RushByteBufferAllocator()
	val byteBuffers = ArrayObjectPool(64, byteBufferAllocator)
	val gate = WebSocketChannelGate(elg, URI(a.address))
	
	val rush = Rush(
		a.version,
		a.activity,
		assistant,
		byteBuffers,
		gate
	) { _: RushUnit, event: RushUnit.Event ->
		when (event) {
			RushUnit.Event.CREATE  -> statistic.add(RushStatistic.Event.UNIT_CREATE)
			RushUnit.Event.OPEN    -> statistic.add(RushStatistic.Event.UNIT_OPEN)
			RushUnit.Event.CLOSE   -> statistic.add(RushStatistic.Event.UNIT_CLOSE)
			is RushUnit.Event.FAIL -> {
				when (event.reason) {
					ConnectFailReason.REFUSED       -> statistic.add(RushStatistic.Event.UNIT_FAIL_REFUSED)
					ConnectFailReason.AUTHORIZATION -> statistic.add(RushStatistic.Event.UNIT_FAIL_AUTHORIZATION)
					ConnectFailReason.VERSION       -> statistic.add(RushStatistic.Event.UNIT_FAIL_VERSION)
					ConnectFailReason.ERROR         -> statistic.add(RushStatistic.Event.UNIT_FAIL_ERROR)
				}
			}
		}
	}
	
	var running = true
//	var stopping = false
	
	
	fun produceUnit() {
		rush.produceUnit(Random.nextInt(1, a.count + 1))
	}
	
	repeat(a.count) {
		produceUnit()
	}
	
	thread {
		val clock = Clock.systemDefaultZone()
		while (running) {
			val activeUnits = rush.activeUnits
//			if (!stopping && activeUnits < a.count) {
//				repeat(a.count - activeUnits) {
//					produceUnit()
//				}
//			}
			println(
				LocalTime.now(clock).toString()
					+ l("create", statistic[RushStatistic.Event.UNIT_CREATE])
					+ l("active", activeUnits)
					+ l("open", statistic[RushStatistic.Event.UNIT_OPEN])
					+ l("close", statistic[RushStatistic.Event.UNIT_CLOSE])
					+ l("fail R", statistic[RushStatistic.Event.UNIT_FAIL_REFUSED])
					+ l("fail A", statistic[RushStatistic.Event.UNIT_FAIL_AUTHORIZATION])
					+ l("fail V", statistic[RushStatistic.Event.UNIT_FAIL_VERSION])
					+ l("fail E", statistic[RushStatistic.Event.UNIT_FAIL_ERROR])
					
					+ l("ch accept", statistic[RushStatistic.Event.CHANNEL_ACCEPT])
					+ l("ch close i", statistic[RushStatistic.Event.CHANNEL_CLOSE_INPUT])
					+ l("ch close o", statistic[RushStatistic.Event.CHANNEL_CLOSE_OUTPUT])
					
					+ l("con accept", statistic[RushStatistic.Event.CONNECTION_ACCEPT])
					+ l("con close i", statistic[RushStatistic.Event.CONNECTION_CLOSE_INPUT])
					+ l("con close o", statistic[RushStatistic.Event.CONNECTION_CLOSE_OUTPUT])
			)
			
			Thread.sleep(1000)
		}
	}
	
	Runtime.getRuntime().addShutdownHook(Thread {
		logger.info("Stop")
		
//		stopping = true
		rush.stop()
		
		while (rush.active) {
			Thread.sleep(100)
		}
		
		logger.info("Stop elg")
		elg.shutdownGracefully().syncUninterruptibly()
		
		logger.info("Stop assistant")
		assistantExecutor.shutdown()
		if (!assistantExecutor.awaitTermination(1, TimeUnit.MINUTES)) {
			logger.warn("Assistant not stopped")
		}
		
		byteBuffers.clear()
		logger.info("ByteBuffers: produces ${byteBufferAllocator.produces}, clears: ${byteBufferAllocator.clears}, disposes: ${byteBufferAllocator.disposes}")
		
		logger.info("Stopped")
		
		running = false
	})
}

fun l(label: String, value: Number): String {
	return " | " + label + value.toString().padStart(5)
}

class RushStatistic {
	enum class Event {
		UNIT_CREATE,
		UNIT_OPEN,
		UNIT_CLOSE,
		UNIT_FAIL_REFUSED,
		UNIT_FAIL_AUTHORIZATION,
		UNIT_FAIL_VERSION,
		UNIT_FAIL_ERROR,
		
		CHANNEL_ACCEPT,
		CHANNEL_CLOSE_OUTPUT,
		CHANNEL_CLOSE_INPUT,
		CONNECTION_ACCEPT,
		CONNECTION_CLOSE_INPUT,
		CONNECTION_CLOSE_OUTPUT,
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
}