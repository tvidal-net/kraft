package uk.tvidal.kraft.storage.config

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.ACTIVE
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.COMMITTED
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.DISCARDED
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.TRUNCATED
import uk.tvidal.kraft.storage.BaseFileTest
import uk.tvidal.kraft.storage.config.FileName.Companion.parseFrom
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class FileNameTest {

    @Test
    internal fun `parse index file name`() {
        assertEquals(FileName("kraft", 5), parseFrom("kraft-0000005.krx"))
    }

    @Test
    internal fun `parse discarded file name`() {
        assertEquals(FileName("abcde", 8, DISCARDED), parseFrom("abcde-8.d.kr"))
    }

    @Test
    internal fun `parse committed file name`() {
        assertEquals(FileName("committed", 3, COMMITTED), parseFrom("committed-003.c.kr"))
    }

    @Test
    internal fun `parse upper case names`() {
        assertEquals(FileName("kraft", 1801), parseFrom("KRAFT-1801.krx"))
    }

    @Test
    internal fun `ignore if pattern is not a match`() {
        assertNull(parseFrom("myfile.kr"))
    }

    @Test
    internal fun `ignore incorrect extension`() {
        assertNull(parseFrom("newfile-001.c.uk"))
    }

    @Test
    internal fun `next file returns the correct id`() {
        val name = FileName("test", 10)
        val next = name.next
        assertEquals(11, actual = next.fileIndex)
        assertEquals("test-11.kr", actual = next.current)
    }

    @Nested
    inner class FileTests : BaseFileTest() {

        val path = dir.toPath()!!

        @Test
        internal fun `renames the file correctly`() {

            val active = FileName("name", 123, ACTIVE)
            active.current(path).createNewFile()
            assertFileExists(active)

            val committed = active.rename(COMMITTED, path)
            assertFileExists(committed)

            val discarded = committed.rename(DISCARDED, path)
            assertFileExists(discarded)

            val truncated = discarded.rename(TRUNCATED, path)
            assertFileExists(truncated)
            assertFileExists(active)
        }

        private fun assertFileExists(name: FileName) {
            assertTrue { name.current(path).exists() }
        }
    }
}
