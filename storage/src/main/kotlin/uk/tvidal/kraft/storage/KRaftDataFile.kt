package uk.tvidal.kraft.storage

import uk.tvidal.kraft.codec.binary.BinaryCodec.DataEntry
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileHeader
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.ACTIVE
import uk.tvidal.kraft.codec.binary.BinaryCodec.IndexEntry
import uk.tvidal.kraft.codec.binary.computeSerialisedSize
import uk.tvidal.kraft.codec.binary.entryOf
import uk.tvidal.kraft.codec.binary.toProto
import uk.tvidal.kraft.logging.KRaftLogging
import java.io.File
import java.nio.ByteBuffer
import java.util.Stack

class KRaftDataFile private constructor(
    buffer: ByteBuffer
) {
    companion object : KRaftLogging() {
        fun open(file: File) = KRaftDataFile(
            openMemoryMappedFile(file, file.length())
        ).apply {
            if (!validateHeader()) {
                throw IllegalStateException("Could not open file:  $file")
            }
        }

        fun create(file: File, fileSize: Long = 1024L, firstIndex: Long = 1L) = KRaftDataFile(
            openMemoryMappedFile(file, fileSize)
        ).apply {
            if (validateHeader()) {
                throw IllegalStateException("Cannot overwrite existing file: $file")
            }
            stream.position = FILE_INITIAL_POSITION
            writeHeader(newFirstIndex = firstIndex)
        }
    }

    val stream = ByteBufferStream(buffer)

    var state: FileState = ACTIVE
        private set

    var count: Int = 0
        private set

    var firstIndex: Long = 0L
        private set

    val lastIndex: Long
        get() = firstIndex + count - 1

    val range: LongRange
        get() = LongRange(firstIndex, lastIndex)

    private val mark = Stack<Int>()

    operator fun contains(index: Long) = index in range

    fun commit(commitIndex: Long) {
        TODO("$commitIndex")
    }

    operator fun get(range: KRaftIndexEntryRange): KRaftEntries = entries(
        range.map { get(it) }
    )

    operator fun get(index: IndexEntry): KRaftEntry = buffer {
        stream.position = index.offset
        entryOf(
            DataEntry
                .parseDelimitedFrom(stream.input)
        )
    }

    fun append(data: KRaftEntries): Iterable<IndexEntry> = sequence {
        try {
            for (entry in data) {
                val proto = entry.toProto()
                val size = computeSerialisedSize(proto)
                if (size <= stream.available) {
                    val index = append(proto)
                    count++

                    yield(index)
                } else break
            }
        } finally {
            writeHeader()
        }
    }.asIterable()

    private fun append(entry: DataEntry): IndexEntry {
        val index = lastIndex + 1
        val offset = stream.position
        entry.writeDelimitedTo(stream.output)
        val bytes = stream.position - offset
        return IndexEntry.newBuilder()
            .setId(entry.id)
            .setIndex(index)
            .setOffset(offset)
            .setBytes(bytes)
            .build()
    }

    fun rebuildIndex(): Iterable<IndexEntry> = object : Iterable<IndexEntry>, Iterator<IndexEntry> {

        private var index = firstIndex
        private var offset = FILE_INITIAL_POSITION

        override fun iterator(): Iterator<IndexEntry> = this
        override fun hasNext(): Boolean = index in range

        override fun next(): IndexEntry = buffer {
            position(offset)
            val proto = DataEntry.parseDelimitedFrom(stream.input)
            val bytes = position() - offset

            val index = IndexEntry.newBuilder()
                .setId(proto.id)
                .setIndex(index++)
                .setOffset(offset)
                .setBytes(bytes)
                .build()

            offset += bytes
            index
        }
    }

    private inline fun <T> buffer(block: ByteBuffer.() -> T): T {
        with(stream.buffer) {
            mark.push(stream.position)
            try {
                return block()
            } finally {
                stream.position = mark.pop()
            }
        }
    }

    private fun validateHeader(): Boolean {
        if (stream.buffer.limit() == 0) return false
        val header = readHeader()
        if (header.magicNumber == KRAFT_MAGIC_NUMBER) {
            count = header.entryCount
            firstIndex = header.firstIndex
            state = header.state
            stream.position = header.offset
            return true
        }
        return false
    }

    private fun readHeader(): FileHeader = buffer {
        position(0)
        FileHeader.parseDelimitedFrom(stream.input)
    }

    private fun writeHeader(
        newCount: Int = count,
        newFirstIndex: Long = firstIndex,
        newState: FileState = state
    ) {
        buffer {
            val offset = stream.position
            stream.position = 0
            FileHeader.newBuilder()
                .setMagicNumber(KRAFT_MAGIC_NUMBER)
                .setFirstIndex(newFirstIndex)
                .setEntryCount(newCount)
                .setState(newState)
                .setOffset(offset)
                .build()
                .writeDelimitedTo(stream.output)

            count = newCount
            firstIndex = newFirstIndex
            state = newState
        }
    }
}
