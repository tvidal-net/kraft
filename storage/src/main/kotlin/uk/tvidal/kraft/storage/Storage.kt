package uk.tvidal.kraft.storage

import uk.tvidal.kraft.codec.binary.BinaryCodec.IndexEntry
import uk.tvidal.kraft.codec.binary.BinaryCodec.UniqueID
import uk.tvidal.kraft.codec.binary.toProto
import uk.tvidal.kraft.storage.config.FileStorageConfig
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

internal val KRAFT_MAGIC_NUMBER: UniqueID = UUID
    .fromString("ACEDBABE-BEEF-F00D-DEAD-180182C0FFEE")
    .toProto()

const val DEFAULT_FILE_NAME = "kraft"
const val DEFAULT_FILE_SIZE = 4L * 1024 * 1024 // 4 MB
const val FILE_INITIAL_POSITION = 48 // allocate a few bytes for the header

val DEFAULT_FILE_PATH = File(System.getProperty("user.dir"))

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

fun fileStorage(
    dir: File = DEFAULT_FILE_PATH,
    name: String = DEFAULT_FILE_NAME,
    size: Long = DEFAULT_FILE_SIZE
) = KRaftFileStorage(
    FileStorageConfig(dir.toPath(), name, size)
)
