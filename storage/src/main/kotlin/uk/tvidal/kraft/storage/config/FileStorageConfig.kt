package uk.tvidal.kraft.storage.config

import uk.tvidal.kraft.FIRST_INDEX
import uk.tvidal.kraft.createLinks
import uk.tvidal.kraft.storage.data.KRaftData
import uk.tvidal.kraft.storage.KRaftFile
import uk.tvidal.kraft.storage.config.FileName.Companion.isValidFileName
import java.nio.file.Path

data class FileStorageConfig(
    val path: Path,
    val fileName: String,
    val fileSize: Long
) {
    internal val firstFileName: FileName
        get() = FileName(fileName)

    internal fun listFiles() = path.toFile()
        .list { _, name -> isValidFileName(name) }
        .mapNotNull(FileName.Companion::parseFrom)
        .map(this::openFile)
        .also { createLinks(it) }

    internal fun openFile(fileName: FileName): KRaftFile {
        val file = fileName.toFile(path)
        val dataFile = KRaftData.open(file)
        return KRaftFile(dataFile, fileName, this)
    }

    internal fun createFile(
        name: FileName = firstFileName,
        firstIndex: Long = FIRST_INDEX
    ): KRaftFile {
        val file = name.toFile(path)
        val dataFile = KRaftData.create(file, fileSize, firstIndex)
        return KRaftFile(dataFile, name, this)
    }
}
