package uk.tvidal.kraft.storage

import uk.tvidal.kraft.codec.binary.BinaryCodec.IndexEntry
import java.util.TreeSet

class KRaftIndexEntryRange internal constructor(
    indexEntries: Iterable<IndexEntry>
) : Iterable<IndexEntry> {

    private val entries = TreeSet<IndexEntry>(indexEntryComparator)
        .apply { addAll(indexEntries) }

    val size: Int
        get() = entries.size

    val firstIndex = entries.first().index

    val lastIndex = entries.last().index

    val range = LongRange(
        start = firstIndex,
        endInclusive = firstIndex + size - 1
    )

    val bytes: Int = entries.sumBy(IndexEntry::getBytes)

    init {
        if (range.last != lastIndex) {
            throw IllegalStateException("There are gaps in the range")
        }
    }

    operator fun contains(index: Long) = index in range

    override fun iterator() = entries.iterator()

    override fun hashCode() = range.hashCode() xor bytes

    override fun equals(other: Any?) = when {
        this === other -> true
        other !is KRaftIndexEntryRange -> false
        else -> range == other.range && bytes == other.bytes
    }

    override fun toString() = "IndexRange[($range) size=$size bytes=$bytes]"
}
