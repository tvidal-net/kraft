package uk.tvidal.kraft.storage

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.tvidal.kraft.longEntries
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RingBufferStorageTest {

    companion object {
        const val SIZE = 16

        val ENTRIES = longEntries(1, 1..3L)
        val SINGLE_ENTRY = entryOf("SINGLE", 2).toEntries()
    }

    lateinit var storage: KRaftStorage

    @BeforeEach
    fun setup() {
        storage = RingBufferStorage(SIZE)
    }

    @Test
    fun `should return zero for term at index zero`() {
        assertEquals(0, storage.termAt(0))

        storage.append(entries = SINGLE_ENTRY)
        assertEquals(0, storage.termAt(0))
    }

    @Test
    fun `should start with initial parameters`() {
        assertState(1, 0, 0)
    }

    @Test
    fun `should update parameters when data is appended`() {
        assertEquals(3, storage.append(entries = ENTRIES))
        assertState(1, 3, 1)
    }

    @Test
    fun `should update first log index when appended past the buffer size`() {
        storage.append(entries = ENTRIES)
        storage.append(entries = longEntries(2, 4..20L))

        assertState(5, 20, 2)
    }

    @Test
    fun `should truncate if append before the last log index`() {
        storage.append(entries = ENTRIES)
        storage.append(longEntries(2, 2..5L), 2)
        assertState(1, 5, 2)
    }

    @Test
    fun `should truncate from first log index`() {
        storage.append(entries = ENTRIES)
        storage.append(SINGLE_ENTRY, 1)
        assertState(1, 1, 2)
    }

    @Test
    fun `should not append before first log index`() {
        assertThrows<IllegalArgumentException> {
            storage.append(entries = longEntries(1, 1..17L))
            storage.append(SINGLE_ENTRY, 1)
        }
    }

    @Test
    fun `should not append after last log index`() {
        assertThrows<IllegalArgumentException> {
            storage.append(ENTRIES, 2)
        }
    }

    @Test
    fun `should read back appended data`() {
        storage.append(entries = ENTRIES)
        assertEquals(ENTRIES, storage.read(1, Int.MAX_VALUE))
    }

    @Test
    fun `should respect the byte limite when reading data`() {
        storage.append(entries = ENTRIES)
        val read = storage.read(1, LONG_BYTES * 2 + 2)
        assertEquals(ENTRIES.take(2), read.toList())
    }

    @Test
    fun `should return empty if entry cannot fit byte limit`() {
        storage.append(entries = ENTRIES)
        assertTrue(storage.read(1, LONG_BYTES - 1).isEmpty)
    }

    @Test
    fun `should not read before first log index`() {
        assertThrows<IllegalArgumentException> {
            storage.append(entries = longEntries(1, 1..17L))
            storage.read(1, Int.MAX_VALUE)
        }
    }

    @Test
    fun `should return empty on read after last log index`() {
        storage.append(entries = ENTRIES)
        assertTrue(storage.read(4, Int.MAX_VALUE).isEmpty)
    }

    private fun assertState(firstLogIndex: Long, lastLogIndex: Long, lastLogTerm: Long) {
        assertEquals(firstLogIndex, storage.firstLogIndex)
        assertEquals(lastLogIndex, storage.lastLogIndex)
        assertEquals(lastLogTerm, storage.lastLogTerm)
        assertEquals(lastLogIndex + 1, storage.nextLogIndex)
    }
}
