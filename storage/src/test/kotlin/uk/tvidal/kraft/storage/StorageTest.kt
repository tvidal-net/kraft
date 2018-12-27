package uk.tvidal.kraft.storage

import uk.tvidal.kraft.FIRST_INDEX
import uk.tvidal.kraft.codec.binary.BinaryCodec.IndexEntry
import uk.tvidal.kraft.codec.binary.computeSerialisedSize
import uk.tvidal.kraft.codec.binary.toProto
import uk.tvidal.kraft.storage.data.KRaftData
import uk.tvidal.kraft.storage.index.IndexEntryRange
import java.util.UUID

const val TEST_SIZE = 11

val testEntry = entryOf("12345678901", 11L)
val testEntryBytes = computeSerialisedSize(testEntry.toProto())
val testFileLength = INITIAL_OFFSET + TEST_SIZE * testEntryBytes
val testEntries = entries()
val testRange = testEntries.toIndex()

fun entries(size: Int = TEST_SIZE) = KRaftEntries(
    (0 until size)
        .map { testEntry }
)

fun rangeOf(vararg entries: IndexEntry) = rangeOf(entries.toList())

fun rangeOf(entries: Iterable<IndexEntry>) = IndexEntryRange(entries)

fun indexRange(
    count: Int,
    firstIndex: Long = FIRST_INDEX,
    initialOffset: Int = INITIAL_OFFSET,
    bytes: Int = TEST_SIZE
) =
    IndexEntryRange(
        (0 until count).map {
            val index = firstIndex + it
            val offset = initialOffset + (bytes * it)
            indexEntry(index, offset, bytes)
        }
    )

fun indexEntry(
    index: Long = 1L,
    offset: Int = INITIAL_OFFSET,
    bytes: Int = TEST_SIZE
): IndexEntry = IndexEntry.newBuilder()
    .setId(UUID.randomUUID().toProto())
    .setIndex(index)
    .setOffset(offset)
    .setBytes(bytes)
    .build()

fun KRaftData.write(count: Int) = append(entries(count))
fun KRaftFile.write(count: Int) = append(entries(count))
fun KRaftFileStorage.writeAt(fromIndex: Long, count: Int) = append(entries(count), fromIndex)

fun KRaftEntries.toIndex(
    fromIndex: Long = FIRST_INDEX,
    initialOffset: Int = INITIAL_OFFSET
) = IndexEntryRange(
    mapIndexed { i, it ->
        IndexEntry.newBuilder()
            .setId(it.id.toProto())
            .setIndex(fromIndex + i)
            .setOffset(initialOffset + i * testEntryBytes)
            .setBytes(testEntryBytes)
            .build()
    }
)
