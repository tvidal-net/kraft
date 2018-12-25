package uk.tvidal.kraft.storage

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class KRaftIndexEntryRangeTest {

    @Test
    internal fun `creates the index range correctly`() {
        // this tests the test method to build the range
        val range = indexRange(3)
        val list = range.toList()
        with(list[0]) {
            assertEquals(1, index)
            assertEquals(0, offset)
            assertEquals(11, bytes)
        }
        with(list[1]) {
            assertEquals(2, index)
            assertEquals(11, offset)
            assertEquals(11, bytes)
        }
        with(list[2]) {
            assertEquals(3, index)
            assertEquals(22, offset)
            assertEquals(11, bytes)
        }
    }

    @Test
    internal fun `an empty range is created for an empty list of entries`() {
        with(KRaftIndexEntryRange(emptyList())) {
            assertEquals(0, size)
            assertEquals(1L, firstIndex)
            assertEquals(0L, lastIndex)
            assertEquals(1L..0, range)
            assertTrue { isEmpty }
        }
    }

    @Test
    internal fun `validates the entries within the range`() {
        assertThrows<IllegalStateException> {
            KRaftIndexEntryRange(
                listOf(
                    indexEntry(3, 0),
                    indexEntry(5, 0)
                )
            )
        }
    }

    @Test
    internal fun `computes the properties`() {
        val range = indexRange(3)
        assertEquals(LongRange(1, 3), range.range)
        assertEquals(33, range.bytes)
        assertEquals(1, range.firstIndex)
        assertEquals(3, range.lastIndex)
        assertEquals(3, range.size)
    }

    @Test
    internal fun `can check the range`() {
        val range = indexRange(5, 3)
        assertFalse { 2 in range }
        assertTrue { 3 in range }
        assertTrue { 7 in range }
        assertFalse { 8 in range }
    }
}
