package uk.tvidal.kraft.storage

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.tvidal.kraft.FIRST_INDEX
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.DISCARDED
import uk.tvidal.kraft.storage.config.mockFileConfig
import kotlin.test.assertEquals

internal class KRaftFileTest {

    @Test
    internal fun `create a file from an arbitrary index`() {
        val firstIndex = 1801L
        val config = mockFileConfig(firstIndex = firstIndex)
        val file = KRaftFile(config)
        assertEquals(firstIndex, actual = file.firstIndex)
    }

    @Nested
    inner class LifeCycle {

        val config = mockFileConfig()
        val file = KRaftFile(config)

        @Test
        internal fun `test kraft file life cycle`() {
            val entries = entries(
                (0 until TEST_SIZE + 2)
                    .map { testEntry }
            )
            val list = entries.toList()

            val appended = file.append(entries)
            assertEquals(TEST_SIZE, actual = appended)

            val single = file[2]
            assertEquals(list[1], actual = single)

            val read = file.read(FIRST_INDEX, Int.MAX_VALUE)
            assertEquals(testEntries, actual = read)

            file.close(DISCARDED)
        }
    }
}
