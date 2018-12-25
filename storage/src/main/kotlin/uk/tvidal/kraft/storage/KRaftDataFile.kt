package uk.tvidal.kraft.storage

import uk.tvidal.kraft.codec.binary.BinaryCodec.DataEntry
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.ACTIVE
import uk.tvidal.kraft.codec.binary.BinaryCodec.IndexEntry
import uk.tvidal.kraft.codec.binary.toProto
import uk.tvidal.kraft.codec.binary.uuid
import uk.tvidal.kraft.logging.KRaftLogging
import java.io.File
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.channels.FileChannel.MapMode.READ_WRITE
import java.nio.file.Files
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.READ
import java.nio.file.StandardOpenOption.WRITE

class KRaftDataFile(
    file: File,
    fileSize: Long,
    val firstLogIndex: Long,
    private val indexAppender: (IndexEntry) -> Unit
) {
    private companion object : KRaftLogging()

    private val stream: ByteBufferStream

    private val buffer: ByteBuffer
        get() = stream.buffer

    var state: FileState = ACTIVE
        private set

    var count: Int = 0
        private set

    init {
        val buffer = (Files.newByteChannel(file.toPath(), CREATE, READ, WRITE) as FileChannel).use {
            it.map(READ_WRITE, 0, fileSize)
        }
        stream = ByteBufferStream(buffer)
    }

    fun commit(commitIndex: Long) {
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
}
