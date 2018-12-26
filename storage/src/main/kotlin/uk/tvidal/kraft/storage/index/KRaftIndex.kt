package uk.tvidal.kraft.storage.index

import uk.tvidal.kraft.FIRST_INDEX
import uk.tvidal.kraft.codec.binary.BinaryCodec.IndexEntry
import uk.tvidal.kraft.storage.MutableIndexRange
import java.io.Closeable
import java.io.File

class KRaftIndex internal constructor(
    private val file: IndexFile
) : MutableIndexRange, Closeable by file {

    constructor(file: File) : this(IndexFileStream(file))

    private val data = LinkedHashMap<Long, IndexEntry>()

    override var range = readFile()

    private fun readFile(): LongRange {
        var first: Long? = null
        var last = 0L
        for (entry in file) {
            data[entry.index] = entry
            if (first == null) first = entry.index
            last = entry.index
        }
        return LongRange(first ?: FIRST_INDEX, last)
    }

    operator fun get(index: Long): IndexEntry = data[index]!!

    fun read(
        fromIndex: Long = firstIndex,
        byteLimit: Int = Int.MAX_VALUE
    ): IndexEntryRange {
        val list = mutableListOf<IndexEntry>()
        var available = byteLimit
        var index = fromIndex
        while (index <= range.last) {
            val entry = data[index++]!!
            val bytes = entry.bytes
            if (available - bytes >= 0) {
                available -= bytes
                list.add(entry)
            } else break
        }
        return IndexEntryRange(list)
    }

    fun append(range: Iterable<IndexEntry>): Int =
        range
            .onEach(this::append)
            .count()

    private fun append(entry: IndexEntry) {
        validateEntry(entry)
        file.write(entry)

        data[entry.index] = entry
        lastIndex++
    }

    fun truncateAt(index: Long) {
        file.truncateAt(index)
        lastIndex = index - 1
    }

    private fun validateEntry(entry: IndexEntry) {
        val index = firstIndex + data.size

        if (entry.index != index)
            throw IllegalArgumentException("Invalid Entry Index: expected=$index actual=${entry.index}")

        if (data.isNotEmpty()) {
            val last = data[range.last]!!
            val offset = last.offset + last.bytes
            if (entry.offset != offset)
                throw IllegalArgumentException("Invalid Entry Offset: expected=$offset actual=${entry.offset}")
        }
    }

    override fun toString() = "IndexFile[($range) open=${file.isOpen}]"
}
