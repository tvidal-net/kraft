package uk.tvidal.kraft.storage

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.tvidal.kraft.codec.binary.BinaryCodec.IndexEntry
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class KRaftIndexEntryRangeTest {

    val array = "Hello World".toByteArray()

    val size = array.size

    @Test
    internal fun `creates the index range correctly`() {
        // this tests the test method to build the range
        val range = range(3)
        val list = range.toList()
        with(list[0]) {
            assertEquals(1, index)
            assertEquals(0, offset)
            assertEquals(size, bytes)
        }
        with(list[1]) {
            assertEquals(2, index)
            assertEquals(11, offset)
            assertEquals(size, bytes)
        }
        with(list[2]) {
            assertEquals(3, index)
            assertEquals(22, offset)
            assertEquals(size, bytes)
        }
    }

    @Test
    internal fun `validates the entries within the range`() {
        assertThrows<IllegalStateException> {
            KRaftIndexEntryRange(
                listOf(
                    entry(3, 0, array),
                    entry(5, 0, array)
                )
            )
        }
    }

    @Test
    internal fun `computes the properties`() {
        val range = range(3)
        assertEquals(LongRange(1, 3), range.range)
        assertEquals(33, range.bytes)
        assertEquals(1, range.firstIndex)
        assertEquals(3, range.lastIndex)
        assertEquals(3, range.size)
    }

    @Test
    internal fun `can check the range`() {
        val range = range(5, 3)
        assertFalse { 2 in range }
        assertTrue { 3 in range }
        assertTrue { 7 in range }
        assertFalse { 8 in range }
    }

    private fun range(count: Int, firstIndex: Long = 1L, initialOffset: Int = 0) = KRaftIndexEntryRange(
        (0 until count).map {
            val index = firstIndex + it
            val offset = initialOffset + (size * it)
            entry(index, offset, array)
        }
    )

    private fun entry(index: Long, offset: Int, data: ByteArray): IndexEntry = IndexEntry.newBuilder()
        .setIndex(index)
        .setOffset(offset)
        .setBytes(data.size)
        .build()
}
