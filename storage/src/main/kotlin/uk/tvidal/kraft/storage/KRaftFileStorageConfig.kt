package uk.tvidal.kraft.storage

import uk.tvidal.kraft.createLinks
import uk.tvidal.kraft.storage.KRaftFileName.Companion.isValidFileName
import java.nio.file.Path

data class KRaftFileStorageConfig(
    val path: Path,
    val fileName: String = "kraft",
    val fileSize: Long = DEFAULT_FILE_SIZE
) {
    internal fun files() = path.toFile()
        .list { _, name -> isValidFileName(name) }
        .mapNotNull(KRaftFileName.Companion::parseFrom)
        .map(this::open)
        .also { createLinks(it) }

    internal fun open(fileName: KRaftFileName): KRaftFile {
        val file = fileName.toFile(path)
        val dataFile = KRaftDataFile.open(file)
        return KRaftFile(dataFile, fileName, this)
    }

    internal fun create(fileName: KRaftFileName, firstIndex: Long): KRaftFile {
        val file = fileName.toFile(path)
        val dataFile = KRaftDataFile.create(file, fileSize, firstIndex)
        return KRaftFile(dataFile, fileName, this)
    }
}
