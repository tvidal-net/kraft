package uk.tvidal.kraft.storage

import uk.tvidal.kraft.codec.binary.BinaryCodec.DataEntry
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileHeader
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.ACTIVE
import uk.tvidal.kraft.codec.binary.BinaryCodec.IndexEntry
import uk.tvidal.kraft.codec.binary.toProto
import uk.tvidal.kraft.codec.binary.uuid
import uk.tvidal.kraft.logging.KRaftLogging
import java.io.File
import java.nio.ByteBuffer

class KRaftDataFile private constructor(
    buffer: ByteBuffer
) {

    companion object : KRaftLogging() {
        fun open(file: File) = KRaftDataFile(
            openMemoryMappedFile(file, file.length())
        ).apply {
            if (!readHeader()) {
                throw IllegalStateException("Could not open file:  $file")
            }
        }

        fun create(file: File, fileSize: Long, firstIndex: Long) = KRaftDataFile(
            openMemoryMappedFile(file, fileSize)
        ).apply {
            if (readHeader()) {
                throw IllegalStateException("Cannot overwrite existing file: $file")
            }
            writeHeader(newFirstIndex = firstIndex)
            buffer.position(FILE_INITIAL_POSITION)
        }
    }

    val stream = ByteBufferStream(buffer)

    private val buffer: ByteBuffer
        get() = stream.buffer

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

    operator fun contains(index: Long) = index in range

    fun commit(commitIndex: Long) {
        TODO("$commitIndex")
    }

    operator fun get(index: IndexEntry): KRaftEntry {
        buffer.position(index.offset)
        val array = ByteArray(index.bytes)
        buffer.get(array)
        val data = DataEntry.parseFrom(array)
        val payload = data.payload.toByteArray()
        val id = uuid(data.id)
        return KRaftEntry(payload, data.term, id)
    }

    fun append(index: Long, data: KRaftEntry): IndexEntry {
        // TODO: Add delimiters so it can be read as a stream if index is lost
        val offset = buffer.position()
        val entry = data.toProto()
        val array = entry.toByteArray()
        val checksum = checksum(array)
        buffer.put(array)
        return IndexEntry.newBuilder()
            .setId(entry.id)
            .setIndex(index)
            .setOffset(offset)
            .setBytes(array.size)
            .setChecksum(checksum)
            .build()
    }

    private fun writeHeader(
        newCount: Int = count,
        newFirstIndex: Long = firstIndex,
        newState: FileState = state
    ) {
        buffer.mark()
        try {
            buffer.position(0)
            FileHeader.newBuilder()
                .setMagicNumber(KRAFT_MAGIC_NUMBER.toProto())
                .setState(newState)
                .setFirstIndex(newFirstIndex)
                .setEntryCount(newCount)
                .build()
                .writeDelimitedTo(stream.output)

            count = newCount
            firstIndex = newFirstIndex
            state = newState
        } finally {
            buffer.reset()
        }
    }

    private fun readHeader(): Boolean {
        if (buffer.limit() == 0) return false
        buffer.mark()
        try {
            val header = FileHeader.parseDelimitedFrom(stream.input)
            if (uuid(header.magicNumber) == KRAFT_MAGIC_NUMBER) {
                count = header.entryCount
                firstIndex = header.firstIndex
                state = header.state
                return true
            }
            return false
        } finally {
            buffer.reset()
        }
    }
}
