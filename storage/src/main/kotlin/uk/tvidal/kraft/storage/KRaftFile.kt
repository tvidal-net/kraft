package uk.tvidal.kraft.storage

import uk.tvidal.kraft.ChainNode
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.DISCARDED
import uk.tvidal.kraft.javaClassName
import uk.tvidal.kraft.logging.KRaftLogging
import uk.tvidal.kraft.storage.config.FileView
import uk.tvidal.kraft.storage.data.DataFile
import java.io.Closeable

class KRaftFile internal constructor(
    val file: FileView
) : Closeable,
    ChainNode<KRaftFile>,
    DataFile by file.data,
    MutableIndexRange by file.data {

    internal companion object : KRaftLogging()

    override var next: KRaftFile? = null

    override var prev: KRaftFile? = null

    val index: Int
        get() = file.name.fileIndex

    init {
        val format = "%,d"
        val bytes = String.format(format, file.data.buffer.position)
        val available = String.format(format, file.data.buffer.available)
        val size = String.format(format, file.data.size)
        log.info { "$javaClassName $this size=$size bytes=$bytes available=$available" }

        val indexRange = file.index.range
        val dataRange = file.data.range
        if (indexRange != dataRange) {
            throw CorruptedFileException("range Data($dataRange) does not match Index($indexRange)")
        }
    }

    operator fun get(index: Long): KRaftEntry {
        val range = file.index[index]
        return file.data[range]
    }

    fun append(entries: KRaftEntries): Int {
        log.debug { "append entries=$entries" }
        val range = file.data.append(entries)
        return file.index.append(range)
    }

    fun read(fromIndex: Long, byteLimit: Int): KRaftEntries {
        val range = file.index.read(fromIndex, byteLimit)
        log.debug { "read $this range=$range fromIndex=$fromIndex byteLimit=$byteLimit" }
        return file.data[range]
    }

    fun close(state: FileState) {
        with(file) {
            if (state == DISCARDED && data.committed) {
                throw ModifyCommittedFileException("Cannot discard a committed file!")
            }
            index.close()
            data.close(state)
            rename(state)
        }
    }

    override fun close() {
        file.index.close()
        file.data.buffer.release()
        log.info { "closed $this" }
    }

    override fun toString() = "${file.name}/$state::$range"
}
