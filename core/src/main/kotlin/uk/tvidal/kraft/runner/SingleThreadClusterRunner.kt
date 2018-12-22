package uk.tvidal.kraft.runner

import uk.tvidal.kraft.config.KRaftConfig
import uk.tvidal.kraft.engine.RaftEngineImpl
import uk.tvidal.kraft.logging.KRaftLogging
import java.util.concurrent.atomic.AtomicReference

internal class SingleThreadClusterRunner(
    clusterConfig: List<KRaftConfig>,
    private val loopTolerance: LoopToleranceController = LoopToleranceController()
) : KRaftRunner, Runnable {

    private companion object : KRaftLogging()

    private val nodes = clusterConfig.map { RaftEngineImpl(it) }

    private val thread = AtomicReference<Thread>()

    private val running: Boolean
        get() = thread.get() != null

    override fun start() {
        if (!running) {
            val newThread = Thread(this, THREAD_NAME)
            if (thread.compareAndSet(null, newThread)) {
                newThread.start()
            }
        }
    }

    override fun stop() {
        if (running) {
            val currentThread = thread.getAndSet(null)
            currentThread?.join()
        }
    }

    override fun run() {
        try {
            loop()
        } catch (e: Throwable) {
            log.error(e) { "SEVERE ERROR: ${e.message}" }
        }
    }

    private fun loop() {
        var nodeIndex = 0
        while (running) {
            val node = nodes[nodeIndex]
            try {
                val now = loopTolerance.yield()
                node.run(now)
            } catch (e: Exception) {
                log.error(e) { "Error while processing: ${node.self}" }
            } finally {
                nodeIndex = (nodeIndex + 1) % nodes.size
            }
        }
    }
}
