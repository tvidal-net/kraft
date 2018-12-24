package uk.tvidal.kraft.storage

import java.nio.file.Path

data class KRaftFileStorageConfig(
    val path: Path,
    val fileName: String = "kraft",
    val fileSize: Int = DEFAULT_FILE_SIZE
)
