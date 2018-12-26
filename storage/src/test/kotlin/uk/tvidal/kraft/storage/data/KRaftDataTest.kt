package uk.tvidal.kraft.storage.data

import com.google.protobuf.InvalidProtocolBufferException.InvalidWireTypeException
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.ACTIVE
import uk.tvidal.kraft.storage.FILE_INITIAL_POSITION
import uk.tvidal.kraft.storage.TEST_SIZE
import uk.tvidal.kraft.storage.buffer.ByteBufferStream
import uk.tvidal.kraft.storage.entries
import uk.tvidal.kraft.storage.entryOf
import uk.tvidal.kraft.storage.index.IndexEntryRange
import uk.tvidal.kraft.storage.testEntries
import uk.tvidal.kraft.storage.testEntryBytes
import uk.tvidal.kraft.storage.testRange
import uk.tvidal.kraft.storage.write
import uk.tvidal.kraft.storage.writeHeader
import kotlin.test.assertEquals

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
            putLong(Long.MAX_VALUE)
            putInt(Int.MAX_VALUE)
            put("NewCorruptedHeader".toByteArray())
        }
        assertThrows<InvalidWireTypeException> {
            KRaftData(buffer)
        }
    }

    @Test
    internal fun `returns an empty range when cannot write`() {
        val fileSize = FILE_INITIAL_POSITION + TEST_SIZE * testEntryBytes
        val buffer = ByteBufferStream(fileSize).writeHeader()
        val data = KRaftData(buffer)
        assertEquals(testRange, actual = data.write())
        assertEquals(IndexEntryRange.EMPTY, actual = data.write())
    }

    @Nested
    inner class OperationsTests {

        val buffer = ByteBufferStream()
            .writeHeader()

        val data = KRaftData(buffer)

        @Test
        internal fun `can read a range of entries`() {
            assertEquals(testRange, actual = data.write())
            assertState(1L..11, ACTIVE)

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
            assertState(1L..0, ACTIVE)
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
            assertState(1L..6)
        }

        @Test
        internal fun `can rebuild index from data`() {
            data.write()
            assertEquals(testRange.toList(), actual = data.rebuildIndex().toList())
        }

        fun assertState(
            range: LongRange? = null,
            state: FileState? = null
        ) {
            if (range != null) {
                assertEquals(range, actual = data.range)
                assertEquals(range.first, actual = data.firstIndex)
                assertEquals(range.start, actual = data.firstIndex)
                assertEquals(range.last, actual = data.lastIndex)
                assertEquals(range.endInclusive, actual = data.lastIndex)
                assertEquals(range.last - range.first + 1, actual = data.size.toLong())
            }

            if (state != null) {
                assertEquals(state, actual = data.state)
            }
        }
    }
}
