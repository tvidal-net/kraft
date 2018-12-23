package uk.tvidal.kraft.server

import uk.tvidal.kraft.config.KRaftConfig
import uk.tvidal.kraft.engine.RaftEngine

internal class MultiThreadClusterServer(
    clusterConfig: List<KRaftConfig>,
    private val loopTolerance: LoopToleranceController = LoopToleranceController()
) : ClusterServer(clusterConfig) {

    private val executor = fixedThreadPool(size)

    override fun start() {
        log.info { "Starting..." }
        val work = nodes.map {
            Runnable { loop(it) }
        }
        work.forEach {
            executor.submit(it)
        }
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
