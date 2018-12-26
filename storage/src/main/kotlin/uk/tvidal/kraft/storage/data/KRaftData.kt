package uk.tvidal.kraft.storage.data

import uk.tvidal.kraft.codec.binary.BinaryCodec.DataEntry
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileHeader
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.ACTIVE
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.TRUNCATED
import uk.tvidal.kraft.codec.binary.BinaryCodec.IndexEntry
import uk.tvidal.kraft.codec.binary.computeSerialisedSize
import uk.tvidal.kraft.codec.binary.entryOf
import uk.tvidal.kraft.codec.binary.toProto
import uk.tvidal.kraft.logging.KRaftLogging
import uk.tvidal.kraft.storage.FILE_INITIAL_POSITION
import uk.tvidal.kraft.storage.KRaftEntries
import uk.tvidal.kraft.storage.KRaftEntry
import uk.tvidal.kraft.storage.MutableIndexRange
import uk.tvidal.kraft.storage.buffer.ByteBufferStream
import uk.tvidal.kraft.storage.entries
import uk.tvidal.kraft.storage.index.IndexEntryRange
import uk.tvidal.kraft.storage.isValid
import uk.tvidal.kraft.storage.readHeader
import uk.tvidal.kraft.storage.writeHeader
import java.io.File

class KRaftData internal constructor(
    val buffer: ByteBufferStream
) : MutableIndexRange, DataFile {

    internal companion object : KRaftLogging() {

        fun open(file: File) = KRaftData(
            ByteBufferStream(file, file.length())
        )

        fun create(file: File, fileSize: Long = 1024L, firstIndex: Long = 1L) = KRaftData(
            ByteBufferStream(file, fileSize)
                .writeHeader(firstIndex)
        )
    }

    init {
        if (!validateHeader()) {
            throw IllegalStateException("Invalid file header: $this")
        }
    }

    override var range: LongRange = LongRange.EMPTY

    override var state: FileState = ACTIVE
        private set

    var size: Int = 0
        private set

    operator fun get(range: IndexEntryRange): KRaftEntries = entries(
        range.map { get(it) }
    )

    operator fun get(index: IndexEntry): KRaftEntry = buffer {
        buffer.position = index.offset
        entryOf(
            DataEntry
                .parseDelimitedFrom(buffer.input)
        )
    }

    fun append(data: KRaftEntries): Iterable<IndexEntry> = sequence {

        if (immutable)
            throw IllegalStateException("Cannot modify files in $state state")

        var count = 0
        try {
            for (entry in data) {
                val index = firstIndex + size + count++
                val proto = entry.toProto()
                val size = computeSerialisedSize(proto)
                if (size <= buffer.available) yield(append(proto, index))
                else break
            }
        } finally {
            if (count > 0) {
                size += count
                writeHeader()
            }
        }
    }.asIterable()

    private fun append(entry: DataEntry, index: Long): IndexEntry {
        val offset = buffer.position
        entry.writeDelimitedTo(buffer.output)
        val bytes = buffer.position - offset
        return IndexEntry.newBuilder()
            .setId(entry.id)
            .setIndex(index)
            .setOffset(offset)
            .setBytes(bytes)
            .build()
    }

    fun rebuildIndex(): Iterable<IndexEntry> = object : Iterable<IndexEntry>, Iterator<IndexEntry> {

        private val range = firstIndex..(firstIndex + size - 1)
        private var index = firstIndex
        private var offset = FILE_INITIAL_POSITION

        override fun iterator(): Iterator<IndexEntry> = this
        override fun hasNext(): Boolean = index in range

        override fun next(): IndexEntry = buffer {
            position(offset)
            val data = DataEntry.parseDelimitedFrom(buffer.input)
            val bytes = position() - offset

            val index = IndexEntry.newBuilder()
                .setId(data.id)
                .setIndex(index++)
                .setOffset(offset)
                .setBytes(bytes)
                .build()

            offset += bytes
            index
        }
    }

    private fun validateHeader(): Boolean {
        if (buffer.isEmpty) return false
        val header = readHeader()
        if (header.isValid()) {
            size = header.entryCount
            firstIndex = header.firstIndex
            state = header.state
            buffer.position = header.offset
            return true
        }
        return false
    }

    private fun readHeader(): FileHeader {
        val header = buffer.readHeader()
        size = header.entryCount
        range = header.firstIndex until header.firstIndex + size
        state = header.state
        return header
    }

    private fun writeHeader(
        newCount: Int = size,
        newState: FileState = state,
        newFirstIndex: Long = firstIndex
    ) {
        buffer.writeHeader(newFirstIndex, newCount, newState)
        size = newCount
        range = newFirstIndex until newFirstIndex + newCount
        state = newState
    }

    override fun truncateAt(index: Long) {
        val count = index - firstIndex + 1
        writeHeader(count.toInt(), TRUNCATED)
    }

    fun close(state: FileState) = writeHeader(newState = state)

    override fun release() = buffer.release()

    override fun toString() = "DataFile[($range) $state size=$size]"
}
