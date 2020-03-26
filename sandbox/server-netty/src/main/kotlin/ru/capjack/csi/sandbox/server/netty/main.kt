package ru.capjack.csi.sandbox.server.netty

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import ru.capjack.csi.core.server.Server
import ru.capjack.csi.sandbox.server.netty.RushStatistic.Event.*
import ru.capjack.csi.transport.netty.server.ServerEventLoopGroupsImpl
import ru.capjack.csi.transport.netty.server.WebSocketChannelGate
import ru.capjack.tool.logging.Logging
import ru.capjack.tool.utils.concurrency.ArrayObjectPool
import ru.capjack.tool.utils.concurrency.ExecutorDelayableAssistant
import java.time.Clock
import java.time.LocalTime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

private class Args(parser: ArgParser) {
	val address by parser.positional("Address")
	val shutdown by parser.storing("Shutdown timeout seconds") { toInt() }.default(1)
	val version by parser.storing("Version") { toInt() }.default(0)
	val activity by parser.storing("Activity timeout seconds") { toInt() }.default(10)
}

fun main(args: Array<String>) {
	val logger = Logging.getLogger("sandbox")
	val a = ArgParser(args).parseInto(::Args)
	
	val statistic = RushStatistic()
	
	logger.info("Start executors")
	val assistantExecutor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors())
	val elg = ServerEventLoopGroupsImpl()
	
	logger.info("Start server on ${a.address}")
	val assistant = ExecutorDelayableAssistant(assistantExecutor)
	val byteBufferAllocator = RushByteBufferAllocator()
	val byteBuffers = ArrayObjectPool(64, byteBufferAllocator)
	val connectionAuthorizer = RushConnectionAuthorizer()
	val connectionAcceptor = RushConnectionAcceptor(assistant, statistic)
	val gate = RushGate(WebSocketChannelGate(elg, a.address), statistic)
	val server = Server(
		assistant,
		byteBuffers,
		connectionAuthorizer,
		connectionAcceptor,
		gate,
		a.shutdown,
		a.version,
		a.activity
	)
	
	var running = true
	
	thread {
		val clock = Clock.systemDefaultZone()
		while (running) {
			Thread.sleep(1000)
			println(
				LocalTime.now(clock).toString()
					+ l("ch", server.channels)
					+ l("con", server.connections)
					
					+ l("ch accept", statistic[CHANNEL_ACCEPT])
					+ l("ch close i", statistic[CHANNEL_CLOSE_INPUT])
					+ l("ch close o", statistic[CHANNEL_CLOSE_OUTPUT])
					
					+ l("ch data i", statistic.pull(CHANNEL_DATA_INPUT))
					+ l("ch data o", statistic.pull(CHANNEL_DATA_OUTPUT))
					
					+ l("con accept", statistic[CONNECTION_ACCEPT])
					+ l("con close i", statistic[CONNECTION_CLOSE_INPUT])
					+ l("con close o", statistic[CONNECTION_CLOSE_OUTPUT])
			)
		}
	}
	
	Runtime.getRuntime().addShutdownHook(Thread {
		logger.info("Stop server")
		server.stop()
		
		logger.info("Stop elg")
		elg.stop()
		
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
	return " | " + label + value.toString().padStart(7)
}