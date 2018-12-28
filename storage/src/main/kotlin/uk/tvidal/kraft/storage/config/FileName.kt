package uk.tvidal.kraft.storage.config

import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.COMMITTED
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.DISCARDED
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.WRITABLE
import uk.tvidal.kraft.logging.KRaftLogging
import java.io.File
import java.lang.String.CASE_INSENSITIVE_ORDER
import java.nio.file.Path
import kotlin.text.RegexOption.IGNORE_CASE

data class FileName(
    val name: String,
    val fileIndex: Int = 1,
    val state: FileState = WRITABLE
) : Comparable<FileName> {

    companion object : KRaftLogging() {

        private val COMPARATOR: Comparator<String> = CASE_INSENSITIVE_ORDER

        private const val FORMAT = "%s-%d%s"

        private const val DATA_FILE = ".kr"
        private const val INDEX_FILE = ".krx"

        private const val COMMITTED_EXT = ".c"
        private const val DISCARDED_EXT = ".d"

        private val regex = Regex("(\\w+)-(\\d+)(\\.\\w)?($INDEX_FILE?)$", IGNORE_CASE)

        private val extensionParse = mapOf(
            COMMITTED_EXT to COMMITTED,
            DISCARDED_EXT to DISCARDED
        )

        private val extension = mapOf(
            COMMITTED to "$COMMITTED_EXT$DATA_FILE",
            DISCARDED to "$DISCARDED_EXT$DATA_FILE"
        )

        fun parseFrom(fileName: String): FileName? {
            try {
                val match = regex.matchEntire(fileName.toLowerCase())
                if (match != null) {
                    return FileName(
                        name = match.groupValues[1],
                        fileIndex = match.groupValues[2].toInt(),
                        state = extensionParse[match.groupValues[3]] ?: WRITABLE
                    )
                }
            } catch (e: Exception) {
                log.warn { "Could not parse fileName: $fileName" }
            }
            return null
        }

        fun isValidFileName(fileName: String, name: String): Boolean {
            val match = regex.matchEntire(fileName)
            return match?.run {
                equals(name, groupValues[1]) &&
                    equals(DATA_FILE, groupValues[4])
            } ?: false
        }

        private fun equals(s1: String, s2: String): Boolean =
            COMPARATOR.compare(s1, s2) == 0
    }

    val current: String
        get() = fullName(extension[state])

    val index: String
        get() = fullName(INDEX_FILE)

    val next: FileName
        get() = copy(fileIndex = fileIndex + 1, state = WRITABLE)

    fun current(path: Path): File = path.resolve(current).toFile()

    fun index(path: Path): File = path.resolve(index).toFile()

    private fun fullName(extension: String?): String =
        String.format(FORMAT, name, fileIndex, extension ?: DATA_FILE).toLowerCase()

    internal fun rename(state: FileState, path: Path): FileName {
        val new = copy(state = state)
        current(path).renameTo(new.current(path))
        return new
    }

    override fun compareTo(other: FileName): Int = fileIndex.compareTo(other.fileIndex)

    override fun toString() = current
}
