package uk.tvidal.kraft.storage.data

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.ACTIVE
import uk.tvidal.kraft.storage.BaseFileTest
import uk.tvidal.kraft.storage.FILE_INITIAL_POSITION
import uk.tvidal.kraft.storage.buffer.release
import uk.tvidal.kraft.storage.createDataFile
import uk.tvidal.kraft.storage.entries
import uk.tvidal.kraft.storage.entryOf
import uk.tvidal.kraft.storage.testEntries
import uk.tvidal.kraft.storage.testRange
import java.io.File
import kotlin.test.assertEquals

internal class KRaftDataTest : BaseFileTest() {

    private val existing = KRaftData.open(existingFile)

    /*
        Test Script:
        - Create new file
            - Existing File
        - Open existing
            - Invalid Header
        - Release
            - Read After Release
            - Append After Release

        - Append
            - Without available space
        - Read single
        - Read range
        - Rebuild Index
        - Close (Commit/Truncate/Discard)
            - Write after closed
            - Truncate after closed

        Test File:
        - Fixed size entries
        - 10 entries per file

        Validate State changes:
        - firstIndex, lastIndex, range
        - fileState (immutable, committed)
     */

    @AfterEach
    internal fun tearDown() {
        existing.buffer.buffer.release()
    }

    @Test
    internal fun `can read entries from file`() {
        assertEquals(testEntries, existing[testRange])
    }

    @Test
    internal fun `prevent opening of non existing file`() {
        val file = File("$dir/testOpen.kr")
        if (file.exists()) file.delete()

        assertThrows<IllegalStateException> {
            KRaftData.open(file)
        }
    }

    @Test
    internal fun `prevent creation of existing file`() {
        val file = File("$dir/testCreate.kr")
        createDataFile(file)
        assertThrows<IllegalStateException> {
            KRaftData.create(file)
        }
    }

    @Test
    internal fun `read the header after open`() {
        val file = File("$dir/testReadHeader.kr")
        createDataFile(file, 33)

        KRaftData.open(file).also {
            assertEquals(33L, it.firstIndex)
            assertEquals(11, it.size)
            assertEquals(ACTIVE, it.state)
        }
    }

    @Test
    internal fun `test write entries to file`() {
        val file = File("$dir/testAppend.kr")
        val entries = entries(
            entryOf("ABC"),
            entryOf(2L),
            entryOf("MyTest"),
            entryOf(5L),
            entryOf("DataDataData"),
            entryOf(0xDEAD_EEL)
        )

        var index = 1L
        var offset = FILE_INITIAL_POSITION
        KRaftData.create(file).append(entries).forEach {
            assertEquals(index++, it.index)
            assertEquals(offset, it.offset)
            offset += it.bytes
        }
    }

    companion object {
        val existingFile = File("$dir/existingDataFile.kr")

        @JvmStatic
        @BeforeAll
        internal fun setUp() {
            createDataFile(existingFile)
        }
    }
}
