package uk.tvidal.kraft.streaming

import uk.tvidal.kraft.logging.KRaftLogging
import uk.tvidal.kraft.storage.KRaftEntries
import java.util.LinkedList

class DataWindow(
    val total: Int
) {
    private companion object : KRaftLogging()

    private val data = LinkedList<InflightData>()

    val consumed: Int
        get() = data.sumBy(InflightData::bytes)

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
        log.trace { "consuming ${entries.bytes} bytes $this" }
    }

    fun release(index: Long) {
        data.removeIf { it.lastIndex <= index }
        log.trace { "released up to $index $this" }
    }

    fun reset() = data.clear()

    override fun toString() = "Window[consumed=$consumed available=$available]"
}
