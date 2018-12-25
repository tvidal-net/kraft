package uk.tvidal.kraft.storage

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import java.io.File

internal open class BaseFileTest {

    companion object {

        val dir = File("/tmp/kraftFileTests")

        @BeforeAll
        @JvmStatic
        fun createDirectory() {
            if (dir.exists()) dir.deleteRecursively()
            dir.mkdirs()
        }

        @AfterAll
        @JvmStatic
        fun deleteDirectory() {
            dir.deleteRecursively()
        }
    }
}
