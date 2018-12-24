package uk.tvidal.kraft.streaming

import uk.tvidal.kraft.storage.KRaftEntries
import java.util.LinkedList

class DataWindow(
    val total: Int
) {

    private val data = LinkedList<InflightData>()

    val consumed: Int
        get() = data.sumBy { it.bytes }

    val available: Int
        get() = total - consumed

    fun consume(index: Long, entries: KRaftEntries) {
        data.add(
            InflightData(
                firstIndex = index,
                size = entries.size,
                bytes = entries.bytes
            )
        )
    }

    fun release(index: Long) {
        data.removeIf { it.lastIndex <= index }
    }

    fun reset() = data.clear()
}
