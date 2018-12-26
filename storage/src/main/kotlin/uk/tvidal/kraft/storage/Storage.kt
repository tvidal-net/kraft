package uk.tvidal.kraft.storage

import uk.tvidal.kraft.FIRST_INDEX
import uk.tvidal.kraft.MAGIC_NUMBER
import uk.tvidal.kraft.buffer.ByteBufferStream
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileHeader
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.ACTIVE
import uk.tvidal.kraft.codec.binary.BinaryCodec.IndexEntry
import uk.tvidal.kraft.codec.binary.BinaryCodec.UniqueID
import uk.tvidal.kraft.codec.binary.toProto
import uk.tvidal.kraft.storage.config.FileStorageConfig
import java.io.File
import java.util.zip.CRC32

internal val MAGIC_NUMBER_PROTO: UniqueID = MAGIC_NUMBER
    .toProto()

const val DEFAULT_FILE_NAME = "kraft"
const val DEFAULT_FILE_SIZE = 1024L * 1024 // 1 MB
const val INITIAL_OFFSET = 48 // allocate a few bytes for the header

val DEFAULT_FILE_PATH = File(System.getProperty("user.dir"))

fun checksum(data: ByteArray): Int {
    val crc = CRC32()
    crc.update(data)
    return crc.value.toInt()
}

internal val indexEntryComparator: Comparator<IndexEntry> = Comparator { a, b -> (a.index - b.index).toInt() }

fun fileStorage(
    dir: File = DEFAULT_FILE_PATH,
    name: String = DEFAULT_FILE_NAME,
    size: Long = DEFAULT_FILE_SIZE
) = KRaftFileStorage(
    FileStorageConfig(dir.toPath(), name, size)
)

fun FileHeader.isValid(): Boolean = hasMagicNumber() && magicNumber == MAGIC_NUMBER_PROTO

fun ByteBufferStream.readHeader(): FileHeader = this {
    position = 0
    FileHeader.parseDelimitedFrom(input)
}

fun ByteBufferStream.writeHeader(
    firstIndex: Long = FIRST_INDEX,
    entryCount: Int = 0,
    state: FileState = ACTIVE,
    offset: Int = position
): ByteBufferStream = this {

    val actualOffset = if (isEmpty || offset == 0) INITIAL_OFFSET else offset

    position = 0
    FileHeader.newBuilder()
        .setMagicNumber(MAGIC_NUMBER_PROTO)
        .setFirstIndex(firstIndex)
        .setEntryCount(entryCount)
        .setOffset(actualOffset)
        .setState(state)
        .build()
        .writeDelimitedTo(output)

    position = actualOffset
    this@writeHeader
}
