package uk.tvidal.kraft.storage.index

import uk.tvidal.kraft.codec.binary.BinaryCodec.IndexEntry
import uk.tvidal.kraft.storage.indexEntryComparator
import java.util.TreeSet

class MockIndexFile(entries: Collection<IndexEntry> = emptyList()) : IndexFile {

    val data = TreeSet(indexEntryComparator)
        .apply { addAll(entries) }

    override var isOpen: Boolean = false
        private set

    override fun write(entry: IndexEntry) {
        isOpen = true
        data.add(entry)
    }

    override fun truncateAt(index: Long) {
        data.removeIf { it.index < index }
    }

    override fun close() {
        isOpen = false
    }

    override fun iterator() = data.iterator()
}
