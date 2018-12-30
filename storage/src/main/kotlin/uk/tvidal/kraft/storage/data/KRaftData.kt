package uk.tvidal.kraft.storage.data

import uk.tvidal.kraft.buffer.ByteBufferStream
import uk.tvidal.kraft.codec.binary.BinaryCodec.DataEntry
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.TRUNCATED
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.WRITABLE
import uk.tvidal.kraft.codec.binary.BinaryCodec.IndexEntry
import uk.tvidal.kraft.codec.binary.computeSerialisedSize
import uk.tvidal.kraft.codec.binary.entryOf
import uk.tvidal.kraft.codec.binary.toProto
import uk.tvidal.kraft.logging.KRaftLogging
import uk.tvidal.kraft.storage.CorruptedFileException
import uk.tvidal.kraft.storage.INITIAL_OFFSET
import uk.tvidal.kraft.storage.KRaftEntries
import uk.tvidal.kraft.storage.KRaftEntry
import uk.tvidal.kraft.storage.ModifyCommittedFileException
import uk.tvidal.kraft.storage.MutableIndexRange
import uk.tvidal.kraft.storage.WriteToImmutableFileException
import uk.tvidal.kraft.storage.entries
import uk.tvidal.kraft.storage.index.IndexEntryRange
import uk.tvidal.kraft.storage.isValid
import uk.tvidal.kraft.storage.readHeader
import uk.tvidal.kraft.storage.writeHeader
import java.io.File

class KRaftData internal constructor(
    val buffer: ByteBufferStream
) : MutableIndexRange, DataFile {

    companion object : KRaftLogging() {

        fun open(file: File) = KRaftData(
            ByteBufferStream(file, file.length())
        )

        fun create(file: File, fileLength: Long, firstIndex: Long) = KRaftData(
            ByteBufferStream(file, fileLength)
                .writeHeader(firstIndex)
        )
    }

    override var range: LongRange = LongRange.EMPTY

    override var state: FileState = WRITABLE
        private set

    init {
        if (!validateHeader()) {
            throw CorruptedFileException("Invalid file header: $this")
        }
        log.debug { this }
    }

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

    private fun ensureWritable() {
        if (immutable) {
            throw WriteToImmutableFileException("Cannot modify files in $state state")
        }
    }

    private fun ensureNotCommitted() {
        if (committed) {
            throw ModifyCommittedFileException("Cannot modify files in $state state")
        }
    }

    fun append(data: KRaftEntries) = IndexEntryRange(
        data.also { ensureWritable() }
            .mapIndexedNotNull { i, entry ->
                val index = firstIndex + size + i
                val proto = entry.toProto()
                val size = computeSerialisedSize(proto)
                if (size <= buffer.available) append(proto, index)
                else null
            }
            .also {
                if (it.isNotEmpty())
                    writeHeader(size + it.size)
            }
    )

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
        private var offset = INITIAL_OFFSET

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
        val header = buffer.readHeader()
        if (header.isValid()) {
            firstIndex = header.firstIndex
            size = header.entryCount
            state = header.state
            buffer.position = header.offset
            return true
        }
        return false
    }

    private fun writeHeader(
        newSize: Int = size,
        newState: FileState = state
    ) {
        buffer.writeHeader(firstIndex, newSize, newState)
        size = newSize
        state = newState
    }

    override fun truncateAt(index: Long) {
        ensureNotCommitted()
        val count = index - firstIndex
        writeHeader(count.toInt(), TRUNCATED)
    }

    fun close(state: FileState) {
        ensureNotCommitted()
        writeHeader(newState = state)
    }

    override fun release() = buffer.release()

    override fun toString() = "DataFile[$range $state size=$size available=${buffer.available}]"
}
