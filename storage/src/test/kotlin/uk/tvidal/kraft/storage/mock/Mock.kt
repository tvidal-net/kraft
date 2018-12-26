package uk.tvidal.kraft.storage.mock

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import uk.tvidal.kraft.FIRST_INDEX
import uk.tvidal.kraft.buffer.ByteBufferStream
import uk.tvidal.kraft.storage.config.FileConfig
import uk.tvidal.kraft.storage.config.FileName
import uk.tvidal.kraft.storage.data.KRaftData
import uk.tvidal.kraft.storage.index.KRaftIndex
import uk.tvidal.kraft.storage.testFileBytes
import uk.tvidal.kraft.storage.writeHeader

internal fun mockFileConfig(
    firstIndex: Long = FIRST_INDEX,
    fileName: FileName = FileName("testKraft")
): FileConfig {

    val buffer = ByteBufferStream(testFileBytes)
        .writeHeader(firstIndex)

    val indexFile = MockIndexFile()

    val data = KRaftData(buffer)
    val index = KRaftIndex(indexFile)

    return mockk<FileConfig>().also {
        every { it.name } returns fileName
        every { it.data } returns data
        every { it.index } returns index
        every { it.close(any()) } just Runs
    }
}
