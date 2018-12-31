package uk.tvidal.kraft.status

import uk.tvidal.kraft.ansi.AnsiColor.BLUE
import uk.tvidal.kraft.ansi.AnsiColor.CYAN
import uk.tvidal.kraft.ansi.AnsiColor.MAGENTA
import uk.tvidal.kraft.ansi.AnsiColor.YELLOW
import uk.tvidal.kraft.ansi.AnsiMove
import uk.tvidal.kraft.ansi.hasAnsiSupport
import uk.tvidal.kraft.ansi.terminalColumns
import uk.tvidal.kraft.engine.RaftEngine
import uk.tvidal.kraft.engine.RaftState
import uk.tvidal.kraft.every
import uk.tvidal.kraft.javaClassName
import uk.tvidal.kraft.server.ClusterServer
import uk.tvidal.kraft.singleThreadPool
import java.lang.String.format

class StatusReporter(private val cluster: ClusterServer) {

    private val executor = singleThreadPool(javaClassName)

    private val width = terminalColumns / cluster.nodes.size

    companion object {
        private const val lines = 4
    }

    init {
        if (hasAnsiSupport) {
            AnsiMove.scroll(lines + 1)
            AnsiMove.up(lines)
            executor.every(100) { report() }
        }
    }

    fun stop() {
        executor.shutdownNow()
    }

    private fun report() {
        val nodes = cluster.nodes.map(RaftEngine::safeState)
        AnsiMove {
            forEachNode(nodes) { node() }
            forEachNode(nodes) { role() }
            forEachNode(nodes) { log() }
            forEachNode(nodes) { commit() }
        }
    }

    private fun RaftState.node() = "Node" to BLUE(self)
    private fun RaftState.role() = "Role" to CYAN(role)
    private fun RaftState.log() = "Index" to YELLOW("$lastLogIndex T$lastLogTerm")
    private fun RaftState.commit() = "Commit" to MAGENTA("$commitIndex (${leaderCommitIndex - commitIndex})")

    private fun forEachNode(nodes: List<RaftState>, block: RaftState.() -> Pair<String, String>) {
        AnsiMove.clearLine()
        nodes.forEachIndexed { i, state ->
            val col = i * width + 1
            AnsiMove.column(col)
            val (label, value) = block(state)
            print(format("%12s: %s", label, value))
        }
        println()
    }
}
