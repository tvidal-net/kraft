package uk.tvidal.kraft.storage

import uk.tvidal.kraft.codec.binary.BinaryCodec.FileHeader
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.ACTIVE
import uk.tvidal.kraft.codec.binary.BinaryCodec.IndexEntry
import uk.tvidal.kraft.codec.binary.BinaryCodec.UniqueID
import uk.tvidal.kraft.codec.binary.toProto
import uk.tvidal.kraft.storage.buffer.ByteBufferStream
import uk.tvidal.kraft.storage.index.IndexEntryRange
import java.io.File
import java.nio.ByteBuffer
import java.util.UUID

const val TEST_SIZE = 11

val testEntry = entryOf("12345678901", 11L)
val testEntryBytes = testEntry.toProto().serializedSize + 1

val testEntries = (0 until TEST_SIZE)
    .map { testEntry }
    .let { entries(it) }

val testRange = indexRange(TEST_SIZE, 1L, FILE_INITIAL_POSITION, testEntryBytes)

fun createDataFile(
    file: File,
    firstIndex: Long = 1L,
    fileState: FileState = ACTIVE,
    magicNumber: UniqueID = KRAFT_MAGIC_NUMBER,
    entries: KRaftEntries = testEntries
) {
    val buffer = ByteBuffer.allocate(1024)
    val stream = ByteBufferStream(buffer)

    stream.position = FILE_INITIAL_POSITION
    entries.map(KRaftEntry::toProto)
        .forEach { it.writeDelimitedTo(stream.output) }

    val offset = stream.position
    stream.position = 0

    FileHeader.newBuilder()
        .setMagicNumber(magicNumber)
        .setEntryCount(entries.size)
        .setFirstIndex(firstIndex)
        .setState(fileState)
        .setOffset(offset)
        .build()
        .writeDelimitedTo(stream.output)

    buffer.flip()
    file.outputStream().use {
        it.write(buffer.array())
    }
}

fun indexRange(count: Int, firstIndex: Long = 1L, initialOffset: Int = 0, bytes: Int = TEST_SIZE) =
    IndexEntryRange(
        (0 until count).map {
            val index = firstIndex + it
            val offset = initialOffset + (bytes * it)
            indexEntry(index, offset, bytes)
        }
    )

fun indexEntry(index: Long = 1L, offset: Int = 0, bytes: Int = TEST_SIZE): IndexEntry = IndexEntry.newBuilder()
    .setId(UUID.randomUUID().toProto())
    .setIndex(index)
    .setOffset(offset)
    .setBytes(bytes)
    .build()
