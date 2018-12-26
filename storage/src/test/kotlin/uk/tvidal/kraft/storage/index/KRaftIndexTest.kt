package uk.tvidal.kraft.storage.index

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.tvidal.kraft.storage.BaseFileTest
import uk.tvidal.kraft.storage.indexRange
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class KRaftIndexTest : BaseFileTest() {

    /*
    * Test Script:
    * - Create New
    *   - Already exists
    * - Open existing
    *   - Does not exist
    *
    * - Append
    * - Truncate
    * - Read single
    * - Read range
    * - Close
    *
    * Assertions:
    * - Range
    * - Closed/Open
    * */

    companion object {

        val existingFile = File("$dir/existingFile.krx")

        @BeforeAll
        @JvmStatic
        internal fun createFile() {
            existingFile.outputStream().use { stream ->
                val range = indexRange(10, 10)
                range.forEach { it.writeDelimitedTo(stream) }
            }
        }
    }

    val newFile = File("$dir/newFile.krx")

    init {
        if (newFile.exists()) newFile.delete()
    }

    @Test
    internal fun `ensures the file is read properly`() {
        KRaftIndex(existingFile).use {
            assertEquals(10L..19, it.range)
            with(it[16]) {
                assertEquals(16, index)
                assertEquals(66, offset)
                assertEquals(11, bytes)
            }

            with(it[19]) {
                assertEquals(19, index)
                assertEquals(99, offset)
                assertEquals(11, bytes)
            }
        }
    }

    @Test
    internal fun `byte limit is respected on read`() {
        KRaftIndex(existingFile).use {
            val emptyRange = it.read(11, 10)
            assertTrue { emptyRange.isEmpty }

            with(it.read(11, 22)) {
                assertFalse { isEmpty }
                assertEquals(2, size)
                assertEquals(11, firstIndex)
                assertEquals(12, lastIndex)
                assertEquals(11L..12, range)
                assertEquals(22, bytes)
            }

            with(it.read(16, 60)) {
                assertFalse { isEmpty }
                assertEquals(4, size)
                assertEquals(16L..19, range)
                assertEquals(44, bytes)
            }
        }
    }

    @Test
    internal fun `ensure appended data has a valid index`() {
        assertThrows<IllegalArgumentException> {
            KRaftIndex(newFile).use {
                val firstRange = indexRange(10, 1L)
                it.append(firstRange)

                val newRange = indexRange(10, 15)
                it.append(newRange)
            }
        }
    }

    @Test
    internal fun `ensure appended data has a valid offset`() {
        assertThrows<IllegalArgumentException> {
            KRaftIndex(newFile).use {
                val firstRange = indexRange(10, 1L)
                it.append(firstRange)

                val newRange = indexRange(10, 11)
                it.append(newRange)
            }
        }
    }

    @Test
    internal fun `allow append if index and offset are correct`() {
        KRaftIndex(newFile).use {
            val firstRange = indexRange(10, 1L)
            it.append(firstRange)

            val newRange = indexRange(10, 11, 110)
            it.append(newRange)

            assertEquals(1L..20, it.range)
        }
    }
}
