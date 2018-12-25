package uk.tvidal.kraft.storage

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.COMMITTED
import java.io.File
import kotlin.test.assertEquals

internal class DataFileTest : BaseFileTest() {

    private val existing = DataFile.open(existingFile)

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
            DataFile.open(file)
        }
    }

    @Test
    internal fun `prevent creation of existing file`() {
        val file = File("$dir/testCreate.kr")
        createDataFile(file)
        assertThrows<IllegalStateException> {
            DataFile.create(file)
        }
    }

    @Test
    internal fun `read the header after open`() {
        val file = File("$dir/testReadHeader.kr")
        createDataFile(file, 33)

        DataFile.open(file).also {
            assertEquals(33L, it.firstIndex)
            assertEquals(11, it.size)
            assertEquals(COMMITTED, it.state)
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
        DataFile.create(file).append(entries).forEach {
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
