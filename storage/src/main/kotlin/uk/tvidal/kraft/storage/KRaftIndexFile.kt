package uk.tvidal.kraft.storage

import uk.tvidal.kraft.codec.binary.BinaryCodec.IndexEntry
import java.io.Closeable
import java.io.File
import java.io.OutputStream

class KRaftIndexFile internal constructor(
    val file: File
) : Closeable {

    var range: LongRange

    private val data = mutableMapOf<Long, IndexEntry>()

    private var outputStream: OutputStream? = null

    init {
        range = read()
    }

    private fun read(): LongRange {
        var first = 0L
        var last = 0L
        file.inputStream().use { stream ->
            do {
                val entry = IndexEntry.parseDelimitedFrom(stream)
                if (entry != null) {
                    data[entry.index] = entry
                    if (first == 0L) first = entry.index
                    last = entry.index
                }
            } while (entry != null)
        }
        return first..last
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

    fun append(entry: IndexEntry) {
        ensureOpen()
        entry.writeDelimitedTo(outputStream)
        range = range.first..entry.index
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
