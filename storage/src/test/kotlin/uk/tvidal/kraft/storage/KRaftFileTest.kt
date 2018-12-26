package uk.tvidal.kraft.storage

import org.junit.jupiter.api.Test
import uk.tvidal.kraft.storage.config.FileStorageConfig

internal class KRaftFileTest : BaseFileTest() {

    val config = FileStorageConfig(
        path = dir.toPath(),
        fileName = "testKraft",
        fileSize = 1024
    )

    @Test
    internal fun test() {
        val file = config.createFile()
        file.append(testEntries)
        file.truncateAt(20)
    }
}
