package uk.tvidal.kraft.storage.index

import uk.tvidal.kraft.codec.binary.BinaryCodec.IndexEntry
import uk.tvidal.kraft.storage.indexEntryComparator
import java.util.TreeSet

class IndexEntryRange(
    indexEntries: Iterable<IndexEntry>
) : Iterable<IndexEntry> {

    companion object {
        val EMPTY = IndexEntryRange(emptyList())
    }

    private val entries = TreeSet<IndexEntry>(indexEntryComparator)
        .apply { addAll(indexEntries) }

    val size: Int
        get() = entries.size

    val isEmpty: Boolean
        get() = entries.isEmpty()

    val firstIndex: Long
    val lastIndex: Long

    val range: LongRange

    val bytes: Int = entries.sumBy(IndexEntry::getBytes)

    init {
        if (entries.isEmpty()) {
            firstIndex = 1
            lastIndex = 0
        } else {
            firstIndex = entries.first().index
            lastIndex = entries.last().index
        }
        range = LongRange(firstIndex, firstIndex + size - 1)
        if (range.last != lastIndex) {
            throw IllegalStateException("There are gaps in the range: expectedLastIndex=${range.last} actual=$lastIndex")
        }
    }

    operator fun contains(index: Long) = index in range

    override fun iterator() = entries.iterator()

    override fun hashCode() = range.hashCode() xor bytes

    override fun equals(other: Any?) = when {
        this === other -> true
        other !is IndexEntryRange -> false
        else -> range == other.range && bytes == other.bytes
    }

    override fun toString() = "IndexRange[($range) size=$size bytes=$bytes]"
}
