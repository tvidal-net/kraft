package uk.tvidal.kraft.storage

import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.COMMITTED
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.DISCARDED
import java.io.File

class KRaftFile(
    val firstIndex: Long,
    name: KRaftFileName,
    config: KRaftFileStorageConfig
) {

    private val path = config.path

    var next: KRaftFile? = null

    var prev: KRaftFile? = null

    var fileName: KRaftFileName = name
        private set

    val file: File
        get() = path.resolve(fileName.current).toFile()

    val dataFile: KRaftDataFile
    val indexFile = KRaftIndexFile(file, firstIndex)

    val range: LongRange
        get() = indexFile.range

    init {
        file.let {
            dataFile = if (it.exists()) KRaftDataFile.open(it)
            else KRaftDataFile.create(it, config.fileSize, firstIndex)
        }

        val index = path.resolve(fileName.index).toFile()
        if (!index.exists()) {
            indexFile.append(dataFile.rebuildIndex())
            if (dataFile.immutable) indexFile.close()
        }
    }

    operator fun contains(index: Long) = index in range

    operator fun get(index: Long): KRaftEntry = dataFile[indexFile[index]]

    fun append(entries: KRaftEntries): Int = indexFile.append(dataFile.append(entries))

    fun read(index: Long, byteLimit: Int): KRaftEntries = dataFile[indexFile.read(index, byteLimit)]

    fun truncateAt(index: Long) {
        if (dataFile.immutable)
            throw IllegalStateException("Cannot truncate behind the committed data")
    }

    fun close(state: FileState) {
        if (state == DISCARDED && dataFile.state == COMMITTED)
            throw IllegalStateException("Cannot discard a committed file!")

        indexFile.close()
        fileName = fileName.copy(state = state)
        file.renameTo(path.resolve(fileName.current).toFile())
    }
}
