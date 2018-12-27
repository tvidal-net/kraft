package uk.tvidal.kraft.storage

import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.tvidal.kraft.FIRST_INDEX
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.COMMITTED
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.DISCARDED
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.TRUNCATED
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.WRITABLE
import uk.tvidal.kraft.storage.config.mockFile
import uk.tvidal.kraft.storage.config.mockFileFactory
import uk.tvidal.kraft.storage.config.mockFileName
import kotlin.test.assertEquals

internal class KRaftFileStorageTest {

    @Test
    internal fun `removes discarded files on open`() {
        storage(
            1L..11 to COMMITTED,
            12L..20 to DISCARDED,
            12L..22 to TRUNCATED,
            23L..30 to DISCARDED,
            23L..33 to TRUNCATED,
            34L..40 to WRITABLE

        ).assertState(
            40L to 6,
            1L..11 to COMMITTED,
            12L..22 to TRUNCATED,
            23L..33 to TRUNCATED,
            34L..40 to WRITABLE
        )
    }

    @Test
    internal fun `considers the last discarded file index`() {
        storage(
            1L..3 to DISCARDED,
            1L..4 to DISCARDED
        ).assertState(
            0L to 3,
            1L..0 to WRITABLE
        )
    }

    @Nested
    inner class TruncateFiles {

        val storage = KRaftFileStorage(
            mockFileFactory()
        )

        @BeforeEach
        internal fun setUp() {
            storage.writeAt(FIRST_INDEX, TEST_SIZE * 3 - 1)
        }

        @Test
        internal fun `truncates file at first index`() {
            with(storage) {
                writeAt(FIRST_INDEX, 2)
                assertFileIndex(2, 4)
                assertFiles(1L..2 to WRITABLE)
            }
        }

        @Test
        internal fun `truncates halfway the second file`() {
            with(storage) {
                writeAt(TEST_SIZE + 2L, 2)
                assertFileIndex(14, 4)
                assertFiles(
                    1L..11 to TRUNCATED,
                    12L..12 to TRUNCATED,
                    13L..14 to WRITABLE
                )
            }
        }

        @Test
        internal fun `truncates just the third file`() {
            with(storage) {
                writeAt(23, 2)
                assertFileIndex(24, 4)
                assertFiles(
                    1L..11 to TRUNCATED,
                    12L..22 to TRUNCATED,
                    23L..24 to WRITABLE
                )
            }
        }

        @Test
        internal fun `append single entry at the third file`() {
            with(storage) {
                writeAt(33, 1)
                assertFileIndex(33, 3)
                assertFiles(
                    1L..11 to TRUNCATED,
                    12L..22 to TRUNCATED,
                    23L..33 to WRITABLE
                )
                writeAt(34, 1)
                assertFileIndex(34, 4)
                assertFile(currentFile, 34L..34 to WRITABLE)
            }
        }
    }

    private fun storage(vararg files: Pair<LongRange, FileState>) = KRaftFileStorage(
        mockFileFactory().also {
            every { it.open() } answers {
                files.mapIndexed { i, entry ->
                    open(i + 1, entry.first, entry.second)
                }
            }
        }
    )

    private fun open(fileIndex: Int, range: LongRange, state: FileState): KRaftFile {
        val config = mockFile(range.first, mockFileName(fileIndex))
        val file = KRaftFile(config)
        file.write(range.size)
        file.close(state)
        return file
    }

    private fun KRaftFileStorage.assertState(
        expectedFileState: Pair<Long, Int>,
        vararg expectedFiles: Pair<LongRange, FileState>
    ) {
        expectedFileState.run { assertFileIndex(first, second) }
        assertFiles(*expectedFiles)
    }

    private fun KRaftFileStorage.assertFiles(
        vararg expectedFiles: Pair<LongRange, FileState>
    ) {
        assertEquals(expectedFiles.size - 1, actual = files.size, message = "files.size")
        files.entries.forEachIndexed { i, it ->
            assertFile(it.value, expectedFiles[i])
        }
        expectedFiles.lastOrNull()?.let {
            assertFile(currentFile, it)
        }
    }

    private fun assertFile(file: KRaftFile, expected: Pair<LongRange, FileState>) {
        assertEquals(expected.first, actual = file.range, message = "range")
        assertEquals(expected.second, actual = file.state, message = "state")
    }

    private fun KRaftFileStorage.assertFileIndex(
        expectedLastIndex: Long,
        expectedFileIndex: Int
    ) {
        assertEquals(FIRST_INDEX, actual = firstLogIndex, message = "firstLogIndex")
        assertEquals(expectedLastIndex, actual = lastLogIndex, message = "lastLogIndex")
        assertEquals(expectedFileIndex, actual = currentFile.index, message = "currentFile.index")
        assertEquals(expectedFileIndex, actual = lastFileIndex, message = "lastFileIndex")
    }
}
