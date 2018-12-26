package uk.tvidal.kraft.storage.index

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.tvidal.kraft.storage.indexRange
import uk.tvidal.kraft.storage.rangeOf
import uk.tvidal.kraft.storage.testEntryBytes
import uk.tvidal.kraft.storage.testRange
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class KRaftIndexTest {

    val indexFile = MockIndexFile()

    val index = KRaftIndex(indexFile)

    @Test
    internal fun `index is open and closed properly`() {
        assertFalse { indexFile.isOpen }
        index.append(testRange)
        assertTrue { indexFile.isOpen }
        index.close()
        assertFalse { indexFile.isOpen }
    }

    @Test
    internal fun `ensures the file is read properly`() {
        val e = testRange.toList()
        index.append(e)
        index.use {
            assertEquals(1L..11, it.range)
            assertEquals(it.read(1L, Int.MAX_VALUE), testRange)
            assertEquals(e[2], it[3])
        }
    }

    @Test
    internal fun `byte limit is respected on read`() {
        val e = testRange.toList()
        index.append(e)
        index.use {
            assertEquals(IndexEntryRange.EMPTY, it.read(6L, 10))
            assertEquals(IndexEntryRange.EMPTY, it.read(12L, Int.MAX_VALUE))
            assertEquals(rangeOf(e[0]), it.read(1L, testEntryBytes * 2 - 1))
            assertEquals(rangeOf(e[2], e[3]), it.read(3L, testEntryBytes * 2 + 1))
            assertEquals(rangeOf(e[10]), it.read(11L, testEntryBytes))
        }
    }

    @Test
    internal fun `ensure appended data has a valid index`() {
        index.use {
            val firstRange = indexRange(10, 1L)
            it.append(firstRange)

            val newRange = indexRange(10, 15)
            assertThrows<IllegalArgumentException> {
                it.append(newRange)
            }
        }
    }

    @Test
    internal fun `ensure appended data has a valid offset`() {
        index.use {
            val firstRange = indexRange(10, 1L)
            it.append(firstRange)

            val newRange = indexRange(10, 11)
            assertThrows<IllegalArgumentException> {
                it.append(newRange)
            }
        }
    }

    @Test
    internal fun `allow append if index and offset are correct`() {
        index.use {
            val firstRange = indexRange(10, 1L)
            it.append(firstRange)

            val newRange = indexRange(10, 11, 110)
            it.append(newRange)

            assertEquals(1L..20, it.range)
        }
    }

    @Test
    internal fun `truncates at the correct position`() {
        val e = testRange.toList()
        index.append(e)
        index.use {
            it.truncateAt(12L)
            assertEquals(testRange, actual = it.read())
            assertEquals(1L..11, actual = it.range)


            it.truncateAt(6L)
            assertEquals(rangeOf(e.subList(3, 5)), actual = it.read(4L))
            assertEquals(1L..5, actual = it.range)


            it.truncateAt(4L)
            assertEquals(rangeOf(e.subList(0, 3)), actual = it.read())

            it.truncateAt(1L)
            assertEquals(IndexEntryRange.EMPTY, actual = it.read())
        }
    }
}
