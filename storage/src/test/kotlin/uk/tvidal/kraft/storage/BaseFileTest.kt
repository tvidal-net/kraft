package uk.tvidal.kraft.storage

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import java.nio.file.Files

internal open class BaseFileTest {
    companion object {
        val dir = Files.createTempDirectory("kraftFileTests").toFile()!!

        @BeforeAll
        @JvmStatic
        fun createDirectory() {
            dir.mkdirs()
        }

        @AfterAll
        @JvmStatic
        fun deleteDirectory() {
            dir.deleteRecursively()
        }
    }
}
