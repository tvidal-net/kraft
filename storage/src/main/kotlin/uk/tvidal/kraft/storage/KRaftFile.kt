package uk.tvidal.kraft.storage

import uk.tvidal.kraft.ChainNode
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.DISCARDED
import uk.tvidal.kraft.storage.config.FileName
import uk.tvidal.kraft.storage.config.FileStorageConfig
import uk.tvidal.kraft.storage.data.KRaftData
import uk.tvidal.kraft.storage.data.DataFileState
import uk.tvidal.kraft.storage.index.KRaftIndex
import java.io.File

class KRaftFile internal constructor(
    val dataFile: KRaftData,
    name: FileName,
    config: FileStorageConfig
) : ChainNode<KRaftFile>,
    DataFileState by dataFile,
    MutableIndexRange by dataFile {

    var fileName: FileName = name
        private set

    private val path = config.path

    private var file: File = path.resolve(fileName.current).toFile()

    override var next: KRaftFile? = null

    override var prev: KRaftFile? = null

    val indexFile = KRaftIndex(
        file = path.resolve(fileName.index).toFile()
    )

    init {
        if (indexFile.isEmpty() && !dataFile.isEmpty()) {
            indexFile.append(dataFile.rebuildIndex())
            if (dataFile.immutable) indexFile.close()
        }
    }

    operator fun get(index: Long): KRaftEntry = dataFile[indexFile[index]]

    fun append(entries: KRaftEntries): Int = indexFile.append(dataFile.append(entries))

    fun read(index: Long, byteLimit: Int): KRaftEntries = dataFile[indexFile.read(index, byteLimit)]

    fun close(state: FileState) {
        if (state == DISCARDED && committed)
            throw IllegalStateException("Cannot discard a committed file!")

        indexFile.close()
        dataFile.close(state)

        fileName = fileName.copy(state = state)
        file.renameTo(path.resolve(fileName.current).toFile())
    }

    override fun toString() = "File[($range) $fileName $state]"
}
