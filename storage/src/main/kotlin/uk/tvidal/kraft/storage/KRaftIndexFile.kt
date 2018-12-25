package uk.tvidal.kraft.storage

import uk.tvidal.kraft.codec.binary.BinaryCodec.IndexEntry
import java.io.Closeable
import java.io.File
import java.io.OutputStream

class KRaftIndexFile internal constructor(
    val file: File,
    firstIndex: Long = 1L
) : Closeable {

    var range: LongRange

    private val data = LinkedHashMap<Long, IndexEntry>()

    private var outputStream: OutputStream? = null

    init {
        if (!file.exists()) file.createNewFile()
        range = readFile(firstIndex)
    }

    private fun readFile(firstIndex: Long): LongRange {
        var first: Long? = null
        var last = 0L
        file.inputStream().use { stream ->
            do {
                val entry = IndexEntry.parseDelimitedFrom(stream)
                if (entry != null) {
                    data[entry.index] = entry
                    if (first == null) first = entry.index
                    last = entry.index
                }
            } while (entry != null)
        }
        return LongRange(first ?: firstIndex, last)
    }

    operator fun get(index: Long): IndexEntry? = data[index]

    fun read(fromIndex: Long, byteLimit: Int): KRaftIndexEntryRange {
        val list = mutableListOf<IndexEntry>()
        var size = 0
        var index = fromIndex
        while (index <= range.last) {
            val entry = data[index++]!!
            val bytes = entry.bytes
            if (size + bytes <= byteLimit) {
                size += bytes
                list.add(entry)
            }
        }
        return KRaftIndexEntryRange(list)
    }

    fun append(range: Iterable<IndexEntry>) {
        range.forEach(this::append)
    }

    fun append(entry: IndexEntry) {
        validateEntry(entry)
        ensureOpen()
        entry.writeDelimitedTo(outputStream)
        data[entry.index] = entry
        range = LongRange(range.first, entry.index)
        outputStream?.flush()
    }

    private fun validateEntry(entry: IndexEntry) {
        val index = range.first + data.size

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
        outputStream?.close()
    }
}
