package uk.tvidal.kraft.storage.config

import uk.tvidal.kraft.createLinks
import uk.tvidal.kraft.storage.KRaftFile
import uk.tvidal.kraft.storage.config.FileName.Companion.isValidFileName
import java.nio.file.Path

data class FileFactoryImpl(
    val fileName: String,
    val fileLength: Long,
    val path: Path
) : FileFactory {

    override fun open() = path.toFile()
        .list { _, name -> isValidFileName(name) }
        .mapNotNull(FileName.Companion::parseFrom)
        .map(this::openFile)
        .also { createLinks(it) }
        .associateBy(KRaftFile::range)

    private fun openFile(name: FileName) = KRaftFile(
        file = FileViewImpl(name, Long.MAX_VALUE, fileLength, path)
    )

    override fun create(firstIndex: Long, fileIndex: Int) = KRaftFile(
        file = FileViewImpl(FileName(fileName, fileIndex), firstIndex, fileLength, path)
    )

    override fun toString() = "${javaClass.simpleName}[$fileName fileLength=$fileLength path=$path]"
}