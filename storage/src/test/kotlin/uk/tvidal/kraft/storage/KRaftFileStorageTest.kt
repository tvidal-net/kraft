package uk.tvidal.kraft.storage

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import uk.tvidal.kraft.storage.config.FileStorageConfig
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class KRaftFileStorageTest : BaseFileTest() {

    val config = mockk<FileStorageConfig>().also {
        every { it.listFiles() } returns emptyMap()
    }

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
