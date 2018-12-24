package uk.tvidal.kraft.server

import uk.tvidal.kraft.config.KRaftServerConfig
import uk.tvidal.kraft.engine.RaftEngine
import uk.tvidal.kraft.fixedThreadPool

class MultiThreadClusterServer internal constructor(
    clusterConfig: List<KRaftServerConfig>,
    private val loopTolerance: LoopToleranceController = LoopToleranceController()
) : ClusterServer(clusterConfig) {

    private val executor = fixedThreadPool(KRAFT_THREAD_NAME, size)

    override fun start() {
        val work = nodes.map {
            Runnable { loop(it) }
        }
        work.forEach {
            executor.submit(it)
        }
        logo()
    }

    override fun stop() {
        log.info { "Stopping..." }
        executor.shutdownNow()
        log.info { "Done" }
    }

    private fun loop(raft: RaftEngine) {
        val thread = Thread.currentThread()
        while (!thread.isInterrupted) {
            try {
                val now = loopTolerance.yield()
                raft.run(now)
            } catch (e: Exception) {
                log.error(e) { "Error while processing ${raft.self}" }
            }
        }
    }
}
