package uk.tvidal.kraft.storage

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import uk.tvidal.kraft.simpleClassName
import java.io.File
import java.nio.file.Files

internal open class BaseFileTest {

    companion object {

        fun file(block: () -> Unit) = file(block.simpleClassName())

        fun file(namePrefix: String) = File("$dir/$namePrefix.kr")

        val dir = Files.createTempDirectory("kraftTests").toFile()!!

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
