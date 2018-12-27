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

    fun read(fromIndex: Long, byteLimit: Int): IndexEntryRange {
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
        file.write(entry)
        val index = entry.index
        data[index] = entry
        if (range.isEmpty()) range = index..index
        else lastIndex++
    }

    fun truncateAt(index: Long) {
        lastIndex = file.truncateAt(index)
        data.entries.removeIf { it.key !in range }
    }

    override fun toString() = "$file($range${if (file.isOpen) " open" else ""})"
}
