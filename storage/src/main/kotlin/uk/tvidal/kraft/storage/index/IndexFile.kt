package uk.tvidal.kraft.storage.index

import uk.tvidal.kraft.codec.binary.BinaryCodec.IndexEntry
import java.io.Closeable

interface IndexFile : Closeable, Iterable<IndexEntry> {

    val isOpen: Boolean

    fun write(entry: IndexEntry)

    fun truncateAt(index: Long)
}
