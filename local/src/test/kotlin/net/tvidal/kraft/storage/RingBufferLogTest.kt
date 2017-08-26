package net.tvidal.kraft.storage

import net.tvidal.kraft.LONG_BYTES
import net.tvidal.kraft.longEntries
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RingBufferLogTest {

    companion object {
        const val SIZE = 16

        val ENTRIES = longEntries(1, 1..3L)
        val SINGLE_ENTRY = entryOf(2, "SINGLE").toEntries()
    }

    lateinit var log: KRaftLog

    @BeforeMethod
    fun setup() {
        log = RingBufferLog(SIZE)
    }

    @Test
    fun shouldReturnZeroForTermAtIndexZero() {
        assertEquals(0, log.termAt(0))

        log.append(SINGLE_ENTRY)
        assertEquals(0, log.termAt(0))
    }

    @Test
    fun shouldStartWithInitialParameters() {
        assertState(1, 0, 0)
    }

    @Test
    fun shouldUpdateParametersWhenDataIsAppended() {
        assertEquals(3, log.append(ENTRIES))
        assertState(1, 3, 1)
    }

    @Test
    fun shouldUpdateFirstLogIndexWhenAppendedPastTheBufferSize() {
        log.append(ENTRIES)
        log.append(longEntries(2, 4..20L))

        assertState(5, 20, 2)
    }

    @Test
    fun shouldTruncateIfAppendBeforeTheLastLogIndex() {
        log.append(ENTRIES)
        log.append(longEntries(2, 2..5L), 2)
        assertState(1, 5, 2)
    }

    @Test
    fun shouldTruncateFromFirstLogIndex() {
        log.append(ENTRIES)
        log.append(SINGLE_ENTRY, 1)
        assertState(1, 1, 2)
    }

    @Test(expectedExceptions = arrayOf(IllegalArgumentException::class))
    fun shouldNotAppendBeforeFirstLogIndex() {
        log.append(longEntries(1, 1..17L))
        log.append(SINGLE_ENTRY, 1)
    }

    @Test(expectedExceptions = arrayOf(IllegalArgumentException::class))
    fun shouldNotAppendAfterLastLogIndex() {
        log.append(ENTRIES, 2)
    }

    @Test
    fun shouldReadBackAppendedData() {
        log.append(ENTRIES)
        assertEquals(ENTRIES, log.read(1, Int.MAX_VALUE))
    }

    @Test
    fun shouldRespectTheByteLimiteWhenReadingData() {
        log.append(ENTRIES)
        val read = log.read(1, LONG_BYTES * 2 + 2)
        assertEquals(ENTRIES.take(2), read.toList())
    }

    @Test
    fun shouldReturnEmptyIfEntryCannotFitByteLimit() {
        log.append(ENTRIES)
        assertTrue(log.read(1, LONG_BYTES - 1).isEmpty)
    }

    @Test(expectedExceptions = arrayOf(IllegalArgumentException::class))
    fun shouldNotReadBeforeFirstLogIndex() {
        log.append(longEntries(1, 1..17L))
        log.read(1, Int.MAX_VALUE)
    }

    @Test
    fun shouldReturnEmptyOnReadAfterLastLogIndex() {
        log.append(ENTRIES)
        assertTrue(log.read(4, Int.MAX_VALUE).isEmpty)
    }

    private fun assertState(firstLogIndex: Long, lastLogIndex: Long, lastLogTerm: Long) {
        assertEquals(firstLogIndex, log.firstLogIndex)
        assertEquals(lastLogIndex, log.lastLogIndex)
        assertEquals(lastLogTerm, log.lastLogTerm)
        assertEquals(lastLogIndex + 1, log.nextLogIndex)
    }
}
