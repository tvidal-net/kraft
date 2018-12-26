package uk.tvidal.kraft.storage.index

import uk.tvidal.kraft.codec.binary.BinaryCodec.IndexEntry
import java.io.File
import java.io.OutputStream

class IndexFileStream internal constructor(val file: File) : IndexFile {

    private var outputStream: OutputStream? = null

    override val isOpen: Boolean
        get() = outputStream != null

    init {
        if (!file.exists()) file.createNewFile()
    }

    override fun truncateAt(index: Long) {
        close()
        val truncatedFile = File("$file.truncate")
        file.renameTo(truncatedFile)
        use {
            IndexIterator(truncatedFile, index)
                .forEach(this::write)
        }
        truncatedFile.delete()
    }

    override fun write(entry: IndexEntry) {
        ensureOpen()
        outputStream?.let {
            entry.writeDelimitedTo(outputStream)
            it.flush()
        }
    }

    private fun ensureOpen() {
        if (outputStream == null) {
            outputStream = file.outputStream()
        }
    }

    override fun close() {
        outputStream?.let {
            outputStream = null
            it.close()
        }
    }

    override fun iterator(): Iterator<IndexEntry> = IndexIterator(file)

    private class IndexIterator(file: File, val index: Long = Long.MAX_VALUE) : Iterator<IndexEntry> {

        private val inputStream = file.inputStream()
        private var next = read()

        override fun hasNext() = next?.let {
            it.hasId() && it.index < index
        } ?: false || close()

        override fun next(): IndexEntry = next!!.apply { next = read() }

        private fun read(): IndexEntry? = IndexEntry.parseDelimitedFrom(inputStream)

        private fun close(): Boolean {
            inputStream.close()
            return false
        }
    }

    override fun toString() = file.name!!
}
