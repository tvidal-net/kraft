package uk.tvidal.kraft.storage.data

import com.google.protobuf.InvalidProtocolBufferException.InvalidWireTypeException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.ACTIVE
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.COMMITTED
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.DISCARDED
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.TRUNCATED
import uk.tvidal.kraft.buffer.ByteBufferStream
import uk.tvidal.kraft.storage.entries
import uk.tvidal.kraft.storage.entryOf
import uk.tvidal.kraft.storage.index.IndexEntryRange
import uk.tvidal.kraft.storage.testEntries
import uk.tvidal.kraft.storage.testEntryBytes
import uk.tvidal.kraft.storage.testFileBytes
import uk.tvidal.kraft.storage.testRange
import uk.tvidal.kraft.storage.write
import uk.tvidal.kraft.storage.writeHeader
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class KRaftDataTest {

    @Test
    internal fun `cannot open with empty buffer`() {
        assertThrows<IllegalStateException> {
            KRaftData(ByteBufferStream(0))
        }
    }

    @Test
    internal fun `cannot open if buffer has no header`() {
        assertThrows<IllegalStateException> {
            KRaftData(ByteBufferStream())
        }
    }

    @Test
    internal fun `cannot open with corrupted header`() {
        val buffer = ByteBufferStream()
        buffer {
            repeat(128) {
                putLong(Long.MAX_VALUE)
            }
        }
        assertThrows<InvalidWireTypeException> {
            KRaftData(buffer)
        }
    }

    @Test
    internal fun `returns an empty range when cannot write`() {
        val data = KRaftData(
            ByteBufferStream(testFileBytes)
                .writeHeader()
        )
        assertEquals(testRange, actual = data.write())
        assertEquals(IndexEntryRange.EMPTY, actual = data.write())
    }

    @Test
    internal fun `returns a partial range if cannot write everything`() {
        val data = KRaftData(
            ByteBufferStream(testFileBytes - (testEntryBytes * 2.5).toInt())
                .writeHeader()
        )
        val expected = testRange.take(8)
        assertEquals(expected, actual = data.write().toList())
    }

    @Nested
    inner class OperationsTests {

        val buffer = ByteBufferStream()
            .writeHeader()

        val data = KRaftData(buffer)

        @Test
        internal fun `can read a range of entries`() {
            assertEquals(testRange, actual = data.write())
            data.assertState(1L..11, ACTIVE)

            assertEquals(testEntries, actual = data[testRange])
        }

        @Test
        internal fun `can read a single entry`() {
            val index = data.write().toList()
            val entries = testEntries.toList()
            assertEquals(entries[3], actual = data[index[3]])
            assertEquals(entries[7], actual = data[index[7]])
        }

        @Test
        internal fun `read the header after open`() {
            data.assertState(1L..0, ACTIVE)
        }

        @Test
        internal fun `test write entries to file`() {
            val entries = entries(
                entryOf("ABC"),
                entryOf(2L),
                entryOf("MyTest"),
                entryOf(5L),
                entryOf("DataDataData"),
                entryOf(0xDEAD_EEL)
            )

            var index = data.lastIndex + 1
            var offset = buffer.position
            data.append(entries).forEach {
                assertEquals(index++, it.index)
                assertEquals(offset, it.offset)
                offset += it.bytes
            }
            data.assertState(1L..6)
        }
    }

    @Nested
    inner class WithData {

        val buffer = ByteBufferStream()
            .writeHeader()

        val data = KRaftData(buffer)

        @BeforeEach
        internal fun setUp() {
            data.write()
        }

        @Test
        internal fun `can rebuild the index from the data`() {
            assertEquals(testRange.toList(), actual = data.rebuildIndex().toList())
        }

        @Test
        internal fun `commits the file and make it immutable`() {
            data.close(COMMITTED)
            assertTrue { data.committed }
            assertTrue { data.immutable }
            data.assertState(1L..11, COMMITTED)

            assertThrows<IllegalStateException> {
                data.close(COMMITTED)
            }
        }

        @Test
        internal fun `truncates the file correctly`() {
            data.truncateAt(8L)
            data.assertState(1L..7, TRUNCATED)

            assertThrows<IllegalStateException> {
                data.truncateAt(6L)
            }
        }

        @Test
        internal fun `discards the file correctly`() {
            data.close(DISCARDED)
            data.assertState(1L..11, DISCARDED)

            assertThrows<IllegalStateException> {
                data.close(DISCARDED)
            }
        }

        @Test
        internal fun `releases the buffer`() {
            val oldBuffer = buffer.buffer
            data.release()
            assertTrue { buffer.isEmpty }
            assertNotEquals(oldBuffer, buffer.buffer)
        }
    }

    fun KRaftData.assertState(
        expectedRange: LongRange? = null,
        expectedState: FileState? = null
    ) {
        if (expectedRange != null) {
            assertEquals(expectedRange, actual = range)
            assertEquals(expectedRange.first, actual = firstIndex)
            assertEquals(expectedRange.start, actual = firstIndex)
            assertEquals(expectedRange.last, actual = lastIndex)
            assertEquals(expectedRange.endInclusive, actual = lastIndex)
            assertEquals(expectedRange.last - expectedRange.first + 1, actual = size.toLong())
        }

        if (expectedState != null) {
            assertEquals(expectedState, actual = state)
        }
    }
}
