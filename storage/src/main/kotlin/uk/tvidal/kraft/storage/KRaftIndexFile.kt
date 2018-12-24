package uk.tvidal.kraft.storage

import uk.tvidal.kraft.codec.binary.BinaryCodec.IndexEntry
import uk.tvidal.kraft.codec.binary.toProto
import java.io.Closeable
import java.io.File
import java.io.OutputStream
import java.util.UUID

class KRaftIndexFile internal constructor(
    val file: File
) : Closeable {

    private val data = mutableMapOf<Long, IndexEntry>()

    private var outputStream: OutputStream? = null

    init {
        read()
    }

    private fun read() {
        file.inputStream().use { stream ->
            do {
                val entry = IndexEntry.parseDelimitedFrom(stream)
                if (entry != null) {
                    data[entry.index] = entry
                }
            } while (entry != null)
        }
    }

    operator fun get(index: Long): IndexEntry? = data[index]

    fun append(id: UUID, index: Long, offset: Int, size: Int, checksum: Int) = append(
        IndexEntry.newBuilder()
            .setId(id.toProto())
            .setIndex(index)
            .setOffset(offset)
            .setSize(size)
            .setChecksum(checksum)
            .build()
    )

    fun append(entry: IndexEntry) {
        ensureOpen()
        entry.writeDelimitedTo(outputStream)
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
