package uk.tvidal.kraft.storage

import uk.tvidal.kraft.ChainNode
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState
import uk.tvidal.kraft.storage.config.FileConfig
import uk.tvidal.kraft.storage.data.DataFile

class KRaftFile internal constructor(private val file: FileConfig) :
    ChainNode<KRaftFile>,
    DataFile by file.data,
    MutableIndexRange by file.index {

    override var next: KRaftFile? = null

    override var prev: KRaftFile? = null

    operator fun get(index: Long): KRaftEntry {
        val range = file.index[index]
        return file.data[range]
    }

    fun append(entries: KRaftEntries): Int {
        val range = file.data.append(entries)
        return file.index.append(range)
    }

    fun read(fromIndex: Long, byteLimit: Int): KRaftEntries {
        val range = file.index.read(fromIndex, byteLimit)
        return file.data[range]
    }

    fun close(state: FileState) = file.close(state)

    override fun toString() = "File[($range) ${file.name} $state]"
}
