package uk.tvidal.kraft.storage.index

import org.junit.jupiter.api.Test
import uk.tvidal.kraft.storage.BaseFileTest
import uk.tvidal.kraft.storage.testRange
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class IndexFileStreamTest : BaseFileTest() {

    companion object {

        val file = file {}
    }

    val index = IndexFileStream(file)

    @Test
    internal fun `ensure data is written and read from file`() {

        assertFalse { index.isOpen }

        val range = testRange.toList()
        range.forEach(index::write)

        assertTrue { index.isOpen }
        assertEquals(range, actual = index.toList())

        index.close()
        assertFalse { index.isOpen }

        val truncated = range.subList(0, 6)
        index.truncateAt(7)
        assertEquals(truncated, actual = index.toList())

        assertFalse { File("$file.truncate").exists() }
        assertFalse { index.isOpen }
    }
}
