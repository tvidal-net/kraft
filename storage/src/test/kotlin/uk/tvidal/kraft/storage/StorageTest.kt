package uk.tvidal.kraft.storage

import uk.tvidal.kraft.codec.binary.BinaryCodec.FileHeader
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.ACTIVE
import uk.tvidal.kraft.codec.binary.BinaryCodec.IndexEntry
import uk.tvidal.kraft.codec.binary.toProto
import java.io.File

fun createDataFile(file: File, firstIndex: Long = 1L, count: Int = 0, state: FileState = ACTIVE) {
    file.outputStream().use {
        FileHeader.newBuilder()
            .setMagicNumber(KRAFT_MAGIC_NUMBER.toProto())
            .setOffset(FILE_INITIAL_POSITION)
            .setFirstIndex(firstIndex)
            .setEntryCount(count)
            .setState(state)
            .build()
            .writeDelimitedTo(it)

        // Adds trailing space
        it.write(ByteArray(128))
    }
}

fun indexRange(count: Int, firstIndex: Long = 1L, initialOffset: Int = 0, bytes: Int = 11) = KRaftIndexEntryRange(
    (0 until count).map {
        val index = firstIndex + it
        val offset = initialOffset + (bytes * it)
        indexEntry(index, offset, bytes)
    }
)

fun indexEntry(index: Long = 1L, offset: Int = 0, bytes: Int = 8): IndexEntry = IndexEntry.newBuilder()
    .setIndex(index)
    .setOffset(offset)
    .setBytes(bytes)
    .build()
