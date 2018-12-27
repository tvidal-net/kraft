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

const val MOCK_FILE_NAME = "test"

internal fun mockFileName(fileIndex: Int = FIRST_INDEX.toInt()) = FileName(MOCK_FILE_NAME, fileIndex)

internal fun mockFile(
    firstIndex: Long = FIRST_INDEX,
    fileName: FileName = mockFileName()
): FileView {

    val buffer = ByteBufferStream(testFileLength)
        .writeHeader(firstIndex)

    val indexFile = MockIndexFile()

    val data = KRaftData(buffer)
    val index = KRaftIndex(indexFile)

    var name = fileName

    return mockk<FileView>().also {
        every { it.name } answers { name }
        every { it.data } returns data
        every { it.index } returns index
        every { it.rename(any()) } answers {
            name = name.copy(state = firstArg())
        }
    }
}

internal fun mockFileFactory() = mockk<FileFactory>().also {
    every { it.open() } returns emptyList()
    every { it.create(any(), any()) } answers {
        KRaftFile(
            mockFile(
                firstIndex = firstArg(),
                fileName = mockFileName(secondArg())
            )
        )
    }
}
