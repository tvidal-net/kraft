package uk.tvidal.kraft.storage

import com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag
import com.google.protobuf.MessageLite
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

    operator fun contains(index: Long) = index in range

    fun commit(commitIndex: Long) {
        TODO("$commitIndex")
    }

    fun append(data: KRaftEntries) = sequence {
        try {
            for (entry in data) {
                val proto = entry.toProto()
                val size = computeSize(proto)
                if (size <= stream.available) {
                    val index = append(proto)
                    count++

                    yield(index)
                } else break
            }
        } finally {
            writeHeader()
        }
    }

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

    private fun computeSize(entry: MessageLite): Int {
        val messageBytes = entry.serializedSize
        val sizeBytes = computeUInt32SizeNoTag(messageBytes)
        return messageBytes + sizeBytes
    }

    private inline fun <T> buffer(block: ByteBuffer.() -> T): T {
        with(stream.buffer) {
            val mark = stream.position
            try {
                return block()
            } finally {
                stream.position = mark
            }
        }
    }

    private fun validateHeader(): Boolean {
        if (stream.buffer.limit() == 0) return false
        val header = readHeader()
        if (uuid(header.magicNumber) == KRAFT_MAGIC_NUMBER) {
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
                .setMagicNumber(KRAFT_MAGIC_NUMBER.toProto())
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
