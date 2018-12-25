package uk.tvidal.kraft.storage

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.COMMITTED
import java.io.File
import kotlin.test.assertEquals

internal class KRaftDataFileTest : BaseFileTest() {

    @Test
    internal fun `prevent opening of non existing file`() {
        val file = File("$dir/testOpen.kr")
        if (file.exists()) file.delete()

        assertThrows<IllegalStateException> {
            KRaftDataFile.open(file)
        }
    }

    @Test
    internal fun `prevent creation of existing file`() {
        val file = File("$dir/testCreate.kr")
        createDataFile(file)
        assertThrows<IllegalStateException> {
            KRaftDataFile.create(file)
        }
    }

    @Test
    internal fun `read the header after open`() {
        val file = File("$dir/testReadHeader.kr")
        createDataFile(
            file = file,
            firstIndex = 33L,
            count = 5,
            state = COMMITTED
        )

        KRaftDataFile.open(file).also {
            assertEquals(33L, it.firstIndex)
            assertEquals(5, it.count)
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
        KRaftDataFile.create(file).append(entries).forEach {
            assertEquals(index++, it.index)
            assertEquals(offset, it.offset)
            offset += it.bytes
        }
    }
}
