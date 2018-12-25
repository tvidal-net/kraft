package uk.tvidal.kraft.storage

import uk.tvidal.kraft.codec.binary.BinaryCodec.IndexEntry
import java.util.TreeSet

data class KRaftEntryRange(
    val entries: Set<IndexEntry>,
    val bytes: Int,
    val range: LongRange
) : Iterable<IndexEntry> {

    constructor(entries: Collection<IndexEntry>) : this(
        entries = TreeSet(indexEntryComparator).apply { addAll(entries) },
        bytes = entries.sumBy(IndexEntry::getBytes),
        range = entries.first().index.let { it..(it + entries.size - 1) }
    )

    init {
        if (entries.last().index != range.last) {
            throw IllegalStateException("There are gaps in the range")
        }
    }

    val firstIndex: Long
        get() = range.first

    val lastIndex: Long
        get() = range.last

    val size: Int
        get() = entries.size

    operator fun contains(index: Long) = index in range

    override fun iterator() = entries.iterator()
}
