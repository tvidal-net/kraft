package uk.tvidal.kraft.storage

import uk.tvidal.kraft.codec.binary.BinaryCodec.DataEntry
import uk.tvidal.kraft.codec.binary.BinaryCodec.IndexEntry
import uk.tvidal.kraft.codec.binary.toProto
import uk.tvidal.kraft.codec.binary.uuid
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.channels.FileChannel.MapMode.READ_WRITE
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.READ
import java.nio.file.StandardOpenOption.WRITE

class KRaftDataFile(
    file: Path,
    fileSize: Long,
    private val indexAppender: (IndexEntry) -> Unit
) {

    private val buffer: ByteBuffer

    init {
        buffer = (Files.newByteChannel(file, CREATE, READ, WRITE) as FileChannel).use {
            it.map(READ_WRITE, 0, fileSize)
        }
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
