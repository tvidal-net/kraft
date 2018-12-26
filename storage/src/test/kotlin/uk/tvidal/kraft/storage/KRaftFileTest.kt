package uk.tvidal.kraft.storage

import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import uk.tvidal.kraft.FIRST_INDEX
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.DISCARDED
import uk.tvidal.kraft.storage.mock.mockFileConfig
import kotlin.test.assertEquals

internal class KRaftFileTest : BaseFileTest() {

    val config = mockFileConfig()

    val file = KRaftFile(config)

    @Test
    internal fun `test kraft file life cycle`() {
        val entries = entries(
            (0 until TEST_SIZE + 2)
                .map { testEntry }
        )
        val list = entries.toList()

        assertEquals(config.name.next, actual = file.nextFileName)

        val appended = file.append(entries)
        assertEquals(TEST_SIZE, actual = appended)

        val single = file[2]
        assertEquals(list[1], actual = single)

        val read = file.read(FIRST_INDEX, Int.MAX_VALUE)
        assertEquals(testEntries, actual = read)

        file.close(DISCARDED)
        val slot = slot<FileState>()
        verify { file.close(capture(slot)) }
        assertEquals(DISCARDED, actual = slot.captured)
    }
}
