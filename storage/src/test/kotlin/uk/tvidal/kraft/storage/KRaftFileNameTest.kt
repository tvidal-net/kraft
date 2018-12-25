package uk.tvidal.kraft.storage

import org.junit.jupiter.api.Test
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.COMMITTED
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.DISCARDED
import uk.tvidal.kraft.storage.KRaftFileName.Companion.parseFrom
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class KRaftFileNameTest {

    @Test
    internal fun `parse index file name`() {
        assertEquals(KRaftFileName("kraft", 5), parseFrom("kraft-0000005.krx"))
    }

    @Test
    internal fun `parse discarded file name`() {
        assertEquals(KRaftFileName("abcde", 8, DISCARDED), parseFrom("abcde-8.d.kr"))
    }

    @Test
    internal fun `parse committed file name`() {
        assertEquals(KRaftFileName("committed", 3, COMMITTED), parseFrom("committed-003.c.kr"))
    }

    @Test
    internal fun `parse upper case names`() {
        assertEquals(KRaftFileName("kraft", 1801), parseFrom("KRAFT-1801.krx"))
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
