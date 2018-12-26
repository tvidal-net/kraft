package uk.tvidal.kraft.storage.index

import uk.tvidal.kraft.codec.binary.BinaryCodec.IndexEntry
import uk.tvidal.kraft.storage.indexEntryComparator

class MockIndexFile(entries: Collection<IndexEntry> = emptyList()) : IndexFile {

    val data = sortedSetOf(indexEntryComparator, *entries.toTypedArray())

    override var isOpen: Boolean = false
        private set

    override fun write(entry: IndexEntry) {
        isOpen = true
        data.add(entry)
    }

    override fun truncateAt(index: Long): Long {
        data.removeIf { it.index >= index }
        return data.lastOrNull()?.index ?: index-1
    }

    override fun close() {
        isOpen = false
    }

    override fun iterator() = data.iterator()
}
