package uk.tvidal.kraft.server

import uk.tvidal.kraft.config.KRaftConfig
import java.util.concurrent.atomic.AtomicReference

class SingleThreadClusterServer internal constructor(
    clusterConfig: List<KRaftConfig>,
    private val loopTolerance: LoopToleranceController = LoopToleranceController()
) : ClusterServer(clusterConfig), Runnable {

    private val thread = AtomicReference<Thread>()

    val running: Boolean
        get() = thread.get() != null

    override fun start() {
        if (!running) {
            val newThread = singleThread(this)
            if (thread.compareAndSet(null, newThread)) {
                log.info { "Starting..." }
                newThread.start()
            }
        }
    }

    override fun stop() {
        if (running) {
            log.info { "Stopping..." }
            val currentThread = thread.getAndSet(null)
            currentThread?.join()
            log.info { "Done" }
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
            val raft = nodes[nodeIndex]
            try {
                val now = loopTolerance.yield()
                raft.run(now)
            } catch (e: Exception) {
                log.error(e) { "Error while processing: ${raft.self}" }
            } finally {
                nodeIndex = (nodeIndex + 1) % nodes.size
            }
        }
    }
}
