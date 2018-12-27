package uk.tvidal.kraft.storage.config

import io.mockk.every
import io.mockk.mockk
import uk.tvidal.kraft.FIRST_INDEX
import uk.tvidal.kraft.buffer.ByteBufferStream
import uk.tvidal.kraft.storage.KRaftFile
import uk.tvidal.kraft.storage.data.KRaftData
import uk.tvidal.kraft.storage.index.KRaftIndex
import uk.tvidal.kraft.storage.index.MockIndexFile
import uk.tvidal.kraft.storage.testFileLength
import uk.tvidal.kraft.storage.writeHeader
import java.nio.file.Path
import java.nio.file.Paths

private const val MOCK_FILE_NAME = "kraft_test"

private const val MOCK_FILE_PATH = "/tmp/$MOCK_FILE_NAME"

internal fun mockFileConfig(
    fileName: FileName = FileName(MOCK_FILE_NAME),
    path: Path = Paths.get(MOCK_FILE_PATH),
    size: Long = testFileLength.toLong(),
    firstIndex: Long = FIRST_INDEX
): FileConfig {

    val buffer = ByteBufferStream(testFileLength)
        .writeHeader(firstIndex)

    val indexFile = MockIndexFile()

    val data = KRaftData(buffer)
    val index = KRaftIndex(indexFile)

    return mockk<FileConfig>().also {
        every { it.name } returns fileName
        every { it.path } returns path
        every { it.fileLength } returns size
        every { it.data } returns data
        every { it.index } returns index
        every { it.close(any()) } answers {
            index.close()
            data.close(firstArg())
        }
    }
}

internal fun mockFileStorageConfig(
    path: Path = Paths.get(MOCK_FILE_PATH),
    fileName: String = MOCK_FILE_NAME,
    fileSize: Long = testFileLength.toLong()
): FileStorageConfig {
    return mockk<FileStorageConfig>().also {
        every { it.path } returns path
        every { it.fileName } returns fileName
        every { it.fileLength } returns fileSize
        every { it.firstFileName } answers { FileName(fileName) }
        every { it.listFiles() } returns emptyMap()
        every { it.openFile(any()) } answers {
            KRaftFile(mockFileConfig(firstArg(), path, fileSize))
        }
        every { it.createFile(any(), any()) } answers {
            KRaftFile(mockFileConfig(firstArg(), path, fileSize, secondArg()))
        }
    }
}
