package uk.tvidal.kraft.status

import uk.tvidal.kraft.ansi.AnsiColor.BLUE
import uk.tvidal.kraft.ansi.AnsiColor.CYAN
import uk.tvidal.kraft.ansi.AnsiColor.MAGENTA
import uk.tvidal.kraft.ansi.AnsiColor.YELLOW
import uk.tvidal.kraft.ansi.AnsiMove
import uk.tvidal.kraft.ansi.hasAnsiSupport
import uk.tvidal.kraft.ansi.terminalColumns
import uk.tvidal.kraft.engine.RaftEngine
import uk.tvidal.kraft.every
import uk.tvidal.kraft.javaClassName
import uk.tvidal.kraft.server.ClusterServer
import uk.tvidal.kraft.singleThreadPool
import java.lang.String.format

class StatusReporter(private val cluster: ClusterServer) {

    private val executor = singleThreadPool(javaClassName)

    private val width = terminalColumns / cluster.nodes.size

    private val actions = listOf(
        ::node,
        ::role,
        ::storage,
        ::commit
    )

    init {
        if (hasAnsiSupport) {
            repeat(actions.size) { println() }
            executor.every(100) { report() }
        }
    }

    fun stop() {
        executor.shutdownNow()
    }

    private fun report() {
        AnsiMove.save()
        AnsiMove.up(actions.size)
        actions.forEach { it() }
        AnsiMove.restore()
    }

    private fun node() {
        forEachNode {
            it.label("Node")
                .append(BLUE(self))
        }
    }

    private fun role() {
        forEachNode {
            it.label("Role")
                .append(CYAN(role))
        }
    }

    private fun storage() {
        forEachNode {
            it.label("Index")
                .append(YELLOW("$lastLogIndex T$lastLogTerm"))
        }
    }

    private fun commit() {
        forEachNode {
            it.label("Commit")
                .append(MAGENTA("$commitIndex (${leaderCommitIndex - commitIndex})"))
        }
    }

    private fun StringBuilder.label(text: Any, size: Int = 12) = append(format("%${size}s: ", text))

    private fun forEachNode(block: RaftEngine.(StringBuilder) -> Unit) {
        AnsiMove.clearLine()
        cluster.nodes.forEachIndexed { i, raft ->
            val col = i * width + 1
            AnsiMove.column(col)
            val sb = StringBuilder()
            block(raft, sb)
            print(sb)
        }
        println()
    }
}
