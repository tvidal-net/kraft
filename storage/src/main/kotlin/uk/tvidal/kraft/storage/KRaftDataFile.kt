package uk.tvidal.kraft.storage

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
import java.io.File

class KRaftDataFile private constructor(
    val buffer: ByteBufferStream
) : MutableIndexRange, KRaftFileState {

    companion object : KRaftLogging() {

        fun open(file: File) = KRaftDataFile(
            ByteBufferStream(file, file.length())
        ).apply {
            if (!validateHeader()) {
                throw IllegalStateException("Could not open file:  $file")
            }
        }

        fun create(file: File, fileSize: Long = 1024L, firstIndex: Long = 1L) = KRaftDataFile(
            ByteBufferStream(file, fileSize)
        ).apply {
            if (validateHeader()) {
                throw IllegalStateException("Cannot overwrite existing file: $file")
            }
            buffer.position = FILE_INITIAL_POSITION
            writeHeader(newFirstIndex = firstIndex)
        }
    }

    override var range = LongRange.EMPTY

    override var state: FileState = ACTIVE
        private set

    var size: Int = 0
        private set

    operator fun get(range: KRaftIndexEntryRange): KRaftEntries = entries(
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
        if (immutable) {
            throw IllegalStateException("Cannot modify files in $state state")
        }

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
        if (buffer.buffer.limit() == 0) return false
        val header = readHeader()
        if (header.magicNumber == KRAFT_MAGIC_NUMBER) {
            size = header.entryCount
            firstIndex = header.firstIndex
            state = header.state
            buffer.position = header.offset
            return true
        }
        return false
    }

    private fun readHeader(): FileHeader = buffer {
        position(0)
        FileHeader.parseDelimitedFrom(buffer.input)
    }

    private fun writeHeader(
        newCount: Int = size,
        newState: FileState = state,
        newFirstIndex: Long = firstIndex
    ) {
        buffer {
            val offset = buffer.position
            buffer.position = 0
            FileHeader.newBuilder()
                .setMagicNumber(KRAFT_MAGIC_NUMBER)
                .setFirstIndex(newFirstIndex)
                .setEntryCount(newCount)
                .setState(newState)
                .setOffset(offset)
                .build()
                .writeDelimitedTo(buffer.output)

            size = newCount
            firstIndex = newFirstIndex
            state = newState
        }
    }

    override fun truncateAt(index: Long) {
        val count = index - firstIndex + 1
        writeHeader(count.toInt(), TRUNCATED)
    }

    fun close(state: FileState) = writeHeader(newState = state)
}
