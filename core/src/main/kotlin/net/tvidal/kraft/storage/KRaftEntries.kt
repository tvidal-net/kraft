package net.tvidal.kraft.storage

class KRaftEntries internal constructor(
  private val data: Collection<KRaftEntry>
) : Iterable<KRaftEntry> {
    val size = data.size
    val bytes = data.sumBy { it.bytes }
    val isEmpty get() = size == 0
    override fun iterator() = data.iterator()
}

