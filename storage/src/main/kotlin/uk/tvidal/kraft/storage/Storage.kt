package uk.tvidal.kraft.storage

import uk.tvidal.kraft.codec.binary.BinaryCodec.IndexEntry
import java.io.File
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.channels.FileChannel.MapMode
import java.nio.file.Files
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.READ
import java.nio.file.StandardOpenOption.WRITE
import java.util.UUID
import java.util.zip.CRC32

internal val KRAFT_MAGIC_NUMBER: UUID = UUID.fromString("f328feab-43a1-48f5-82ca-25acb702e7ee")

const val DEFAULT_FILE_SIZE = 4L * 1024 * 1024 // 4 MB
const val FILE_NAME_FORMAT = "%s_%d.%s"
const val FILE_EXTENSION = "kr"
const val FILE_EXTENSION_COMMIT = "c.kr"
const val FILE_EXTENSION_DISCARD = "d.kr"
const val MAGIC_NUMBER = 0xFF

fun checksum(data: ByteArray): Int {
    val crc = CRC32()
    crc.update(data)
    return crc.value.toInt()
}

internal val indexEntryComparator: Comparator<IndexEntry> = Comparator { a, b -> (a.index - b.index).toInt() }

internal fun openMemoryMappedFile(file: File, size: Long): ByteBuffer {
    val path = file.toPath()
    val channel = Files.newByteChannel(path, CREATE, READ, WRITE) as FileChannel
    return channel.use {
        it.map(MapMode.READ_WRITE, 0, size)
    }
}
