package uk.tvidal.kraft.server

import uk.tvidal.kraft.config.KRaftServerConfig
import uk.tvidal.kraft.logging.KRaftLogging
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

class SingleThreadClusterServer internal constructor(
    clusterConfig: List<KRaftServerConfig>,
    private val loopTolerance: LoopToleranceController = LoopToleranceController()
) : ClusterServer(clusterConfig) {

    internal companion object : KRaftLogging()

    private val currentThread = AtomicReference<Thread>()

    val running: Boolean
        get() = currentThread.get() != null

    private fun createThread() = thread(name = KRAFT_THREAD_NAME, start = false) {
        try {
            loop()
        } catch (e: Throwable) {
            log.error(e) { "SEVERE ERROR: ${e.message}" }
        }
    }

    override fun start() {
        if (!running) {
            val newThread = createThread()
            if (currentThread.compareAndSet(null, newThread)) {
                newThread.start()
                logo()
            }
        }
    }

    override fun stop() {
        if (running) {
            log.info { "Stopping..." }
            val currentThread = currentThread.getAndSet(null)
            currentThread?.join()
            log.info { "Done" }
        }
    }

    private fun loop() {
        var nodeIndex = 0
        while (running) {
            val raft = nodes[nodeIndex]
            try {
                raft.run(
                    now = loopTolerance.yield()
                )
            } catch (e: Exception) {
                log.error(e) { "Error while processing: ${raft.self}" }
            } finally {
                nodeIndex = (nodeIndex + 1) % nodes.size
            }
        }
    }
}
