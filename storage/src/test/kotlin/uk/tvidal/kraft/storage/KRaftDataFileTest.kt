package uk.tvidal.kraft.storage

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileHeader
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.ACTIVE
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.COMMITTED
import uk.tvidal.kraft.codec.binary.toProto
import java.io.File
import kotlin.test.assertEquals

internal class KRaftDataFileTest {

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
        createFile(file)
        assertThrows<IllegalStateException> {
            KRaftDataFile.create(file, 1024, 1L)
        }
    }

    @Test
    internal fun `read the header after open`() {
        val file = File("$dir/testReadHeader.kr")
        createFile(
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

    private fun createFile(file: File, firstIndex: Long = 1L, count: Int = 0, state: FileState = ACTIVE) {
        file.outputStream().use {
            FileHeader.newBuilder()
                .setMagicNumber(KRAFT_MAGIC_NUMBER.toProto())
                .setFirstIndex(firstIndex)
                .setEntryCount(count)
                .setState(state)
                .build()
                .writeDelimitedTo(it)
        }
    }

    companion object {

        private val dir = File("/tmp/kraftDataFileTest")

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
