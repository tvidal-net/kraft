package uk.tvidal.kraft.storage

import uk.tvidal.kraft.FIRST_INDEX
import uk.tvidal.kraft.createLinks
import uk.tvidal.kraft.storage.FileName.Companion.isValidFileName
import java.nio.file.Path

data class FileStorageConfig(
    val path: Path,
    val fileName: String,
    val fileSize: Long
) {
    internal val firstFileName: FileName
        get() = FileName(fileName)

    internal fun files() = path.toFile()
        .list { _, name -> isValidFileName(name) }
        .mapNotNull(FileName.Companion::parseFrom)
        .map(this::open)
        .also { createLinks(it) }

    internal fun open(fileName: FileName): KRaftFile {
        val file = fileName.toFile(path)
        val dataFile = DataFile.open(file)
        return KRaftFile(dataFile, fileName, this)
    }

    internal fun create(
        name: FileName = firstFileName,
        firstIndex: Long = FIRST_INDEX
    ): KRaftFile {
        val file = name.toFile(path)
        val dataFile = DataFile.create(file, fileSize, firstIndex)
        return KRaftFile(dataFile, name, this)
    }
}
