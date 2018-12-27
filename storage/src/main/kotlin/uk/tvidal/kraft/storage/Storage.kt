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
const val INITIAL_OFFSET = 64 // allocate a few extra bytes for the header

val DEFAULT_FILE_PATH = File(System.getProperty("user.dir"))

fun checksum(data: ByteArray): Int {
    val crc = CRC32()
    crc.update(data)
    return crc.value.toInt()
}

internal val indexEntryComparator = Comparator<IndexEntry> { a, b -> (a.index - b.index).toInt() }

internal val longRangeComparator = Comparator<LongRange> { a, b ->
    when {
        a == b -> 0
        a.first > b.last -> 1
        b.first > a.last -> -1
        else -> throw IllegalStateException("Cannot compare colliding ranges!")
    }
}

fun fileStorage(
    name: String = DEFAULT_FILE_NAME,
    dir: File = DEFAULT_FILE_PATH,
    size: Long = DEFAULT_FILE_SIZE
) = KRaftFileStorage(
    FileStorageConfig(name, size, dir.toPath())
)

fun FileHeader.isValid(): Boolean = hasMagicNumber() && magicNumber == MAGIC_NUMBER_PROTO

fun ByteBufferStream.readHeader(): FileHeader = this {
    position = 0
    FileHeader.parseDelimitedFrom(input)
}

fun ByteBufferStream.writeHeader(
    firstIndex: Long = FIRST_INDEX,
    entryCount: Int = 0,
    state: FileState = ACTIVE
): ByteBufferStream = buffer.run {

    val actualOffset = if (isEmpty || position == 0) INITIAL_OFFSET else position

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
