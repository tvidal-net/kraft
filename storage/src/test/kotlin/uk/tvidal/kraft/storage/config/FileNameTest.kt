package uk.tvidal.kraft.storage.config

import org.junit.jupiter.api.Test
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.COMMITTED
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.DISCARDED
import uk.tvidal.kraft.storage.config.FileName.Companion.parseFrom
import kotlin.test.assertEquals
import kotlin.test.assertNull

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
}
