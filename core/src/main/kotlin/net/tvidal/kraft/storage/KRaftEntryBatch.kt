package net.tvidal.kraft.storage

class KRaftEntryBatch internal constructor(
  private val entries: Collection<KRaftEntry>
) : Iterable<KRaftEntry> {
    val size = entries.size
    val bytes = entries.sumBy { it.bytes }
    override fun iterator() = entries.iterator()
}

