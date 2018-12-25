package uk.tvidal.kraft.storage

import uk.tvidal.kraft.codec.binary.BinaryCodec.IndexEntry
import java.io.Closeable
import java.io.File
import java.io.OutputStream

class IndexFile internal constructor(
    val file: File
) : Closeable, MutableIndexRange {

    override var range = LongRange.EMPTY

    private val data = LinkedHashMap<Long, IndexEntry>()

    private var outputStream: OutputStream? = null

    init {
        if (!file.exists()) file.createNewFile()
        range = readFile(firstIndex)
    }

    val isOpen: Boolean
        get() = outputStream != null

    private fun readFile(firstIndex: Long): LongRange {
        var first: Long? = null
        var last = 0L
        file.inputStream().use { stream ->
            while (true) {
                val entry = IndexEntry.parseDelimitedFrom(stream)
                if (entry != null && entry.hasId()) {
                    data[entry.index] = entry
                    if (first == null) first = entry.index
                    last = entry.index
                } else break
            }
        }
        return LongRange(first ?: firstIndex, last)
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
        validateEntry(entry)
        ensureOpen()

        entry.writeDelimitedTo(outputStream)
        outputStream!!.flush()

        data[entry.index] = entry
        lastIndex++
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

    private fun ensureOpen() {
        if (outputStream == null) {
            outputStream = file.outputStream()
        }
    }

    override fun close() {
        val os = outputStream
        outputStream = null
        os?.close()
    }

    override fun toString() = "IndexFile[($range) isOpen=$isOpen]"
}
