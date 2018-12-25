package uk.tvidal.kraft.storage

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class KRaftFileStorageTest : BaseFileTest() {

    @Test
    internal fun `creates file in empty directory`() {
        val storage = fileStorage(dir)
        with(storage) {
            assertEquals(0, files.size)
            assertEquals(1, currentFile.firstIndex)
            assertTrue { currentFile.isEmpty() }
        }
    }
}
