package uk.tvidal.kraft.storage.config

import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.ACTIVE
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.COMMITTED
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.DISCARDED
import uk.tvidal.kraft.logging.KRaftLogging
import java.io.File
import java.nio.file.Path

data class FileName(
    val name: String,
    val id: Int = 1,
    val state: FileState = ACTIVE
) : Comparable<FileName> {

    companion object : KRaftLogging() {

        private const val FORMAT = "%s-%d%s"

        private const val DATA_FILE = ".kr"
        private const val INDEX_FILE = ".krx"

        private const val COMMITTED_EXT = ".c$DATA_FILE"
        private const val DISCARDED_EXT = ".d$DATA_FILE"

        private val regex = Regex("(\\w+)-(\\d+)(\\.\\w)?$INDEX_FILE?$")

        fun parseFrom(fileName: String): FileName? {
            try {
                val match = regex.matchEntire(fileName.trim().toLowerCase())
                if (match != null) {
                    return FileName(
                        name = match.groupValues[1],
                        id = match.groupValues[2].toInt(),
                        state = when (match.groupValues[3]) {
                            COMMITTED_EXT -> COMMITTED
                            DISCARDED_EXT -> DISCARDED
                            else -> ACTIVE
                        }
                    )
                }
            } catch (e: Exception) {
                log.warn { "Could not parse fileName: $fileName" }
            }
            return null
        }

        fun isValidFileName(fileName: String): Boolean =
            regex.matches(fileName.trim().toLowerCase())
    }

    val current: String
        get() = fullName(
            when (state) {
                COMMITTED -> COMMITTED_EXT
                DISCARDED -> DISCARDED_EXT
                else -> DATA_FILE
            }
        )

    val index: String
        get() = fullName(INDEX_FILE)

    fun current(path: Path): File = path.resolve(current).toFile()

    fun index(path: Path): File = path.resolve(index).toFile()

    private fun fullName(extension: String = ""): String =
        String.format(FORMAT, name, id, extension).toLowerCase()

    override fun compareTo(other: FileName): Int = id.compareTo(other.id)

    override fun toString() = current
}
