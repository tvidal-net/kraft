package uk.tvidal.kraft.storage

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.tvidal.kraft.FIRST_INDEX
import uk.tvidal.kraft.storage.config.mockFileStorageConfig
import kotlin.test.assertEquals

internal class KRaftFileStorageTest {

    @Nested
    inner class EmptyDirectory {

        val config = mockFileStorageConfig()
        val storage = KRaftFileStorage(config)

        @Test
        internal fun `test storage lifecycle`() {
            val count = TEST_SIZE * 2
            var index = storage.write(count)
            assertEquals(FIRST_INDEX, actual = storage.firstLogIndex)
            assertEquals(count.toLong(), actual = storage.lastLogIndex)

            index = storage.write(TEST_SIZE, ++index)
            assertEquals(TEST_SIZE * 3L, actual = index)

            // truncate the current file
            index = storage.write(1, index)

            storage.write(3, index - TEST_SIZE * 2)
        }

        @Test
        internal fun `truncate file at first index`() {
            storage.write(TEST_SIZE * 3)
            storage.write(5, FIRST_INDEX)
        }
    }

    private fun KRaftFileStorage.write(
        count: Int = TEST_SIZE,
        fromIndex: Long = FIRST_INDEX
    ) = append(
        fromIndex = fromIndex,
        entries = entries(
            (0 until count)
                .map { testEntry }
        )
    )
}
