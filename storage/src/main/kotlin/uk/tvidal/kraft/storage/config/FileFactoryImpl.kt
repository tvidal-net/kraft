package uk.tvidal.kraft.storage.config

import uk.tvidal.kraft.storage.KRaftFile
import uk.tvidal.kraft.storage.config.FileName.Companion.isValidFileName
import java.nio.file.Path

data class FileFactoryImpl(
    val fileName: String,
    val fileLength: Long,
    val path: Path
) : FileFactory {

    override fun open() = path.toFile()
        .list { _, it -> isValidFileName(it, fileName) }
        .orEmpty()
        .mapNotNull(FileName.Companion::parseFrom)
        .map(this::openFile)

    private fun openFile(name: FileName) = KRaftFile(
        file = FileViewImpl(name, Long.MAX_VALUE, fileLength, path)
    )

    override fun create(firstIndex: Long, fileIndex: Int) = KRaftFile(
        file = FileViewImpl(FileName(fileName, fileIndex), firstIndex, fileLength, path)
    )

    override fun toString() = "$fileName[fileLength=$fileLength path=$path]"
}
