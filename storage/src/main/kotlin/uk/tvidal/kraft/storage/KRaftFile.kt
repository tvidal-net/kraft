package uk.tvidal.kraft.storage

import uk.tvidal.kraft.ChainNode
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.DISCARDED
import java.io.File

class KRaftFile internal constructor(
    val dataFile: KRaftDataFile,
    name: KRaftFileName,
    config: KRaftFileStorageConfig
) : ChainNode<KRaftFile>,
    KRaftFileState by dataFile,
    MutableIndexRange by dataFile {

    var fileName: KRaftFileName = name

    private val path = config.path

    private var file: File = path.resolve(fileName.current).toFile()

    override var next: KRaftFile? = null

    override var prev: KRaftFile? = null

    val indexFile = KRaftIndexFile(file, dataFile.firstIndex)

    init {
        val index = path.resolve(fileName.index).toFile()
        if (!index.exists()) {
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
}
