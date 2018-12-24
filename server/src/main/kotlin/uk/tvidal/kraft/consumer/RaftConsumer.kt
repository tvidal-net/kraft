package uk.tvidal.kraft.consumer

import uk.tvidal.kraft.RaftNode

class RaftConsumer(
    val node: RaftNode,
    var index: Long,
    commitIndex: Long
) {
    var streaming = false

    init {
        update(index, commitIndex)
    }

    fun update(newIndex: Long, commitIndex: Long): Boolean = (newIndex > commitIndex).also {
        index = newIndex
        streaming = it
    }

    override fun toString() = "Consumer[$node index=$index streaming=$streaming]"
}
