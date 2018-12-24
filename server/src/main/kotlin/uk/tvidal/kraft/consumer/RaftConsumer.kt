package uk.tvidal.kraft.consumer

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.streaming.DataWindow
import kotlin.math.max

class RaftConsumer(
    val node: RaftNode,
    var index: Long,
    commitIndex: Long,
    maxBytes: Int
) {
    var streaming = false

    val window = DataWindow(maxBytes)

    init {
        update(index, commitIndex)
    }

    fun update(newIndex: Long, commitIndex: Long): Boolean = (newIndex > commitIndex).also {
        index = max(index, newIndex)
        streaming = it
    }

    override fun toString() = "Consumer[$node index=$index streaming=$streaming]"
}
