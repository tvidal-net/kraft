package net.tvidal.kraft.storage

class SingleEntryBatch<out T : KRaftEntry>(singleEntry: T) : KRaftEntryBatch<KRaftEntry> {

    override val entries: List<T> = listOf(singleEntry)

    companion object {
        fun empty(term: Long) = SingleEntryBatch(EmptyEntry(term))
    }

}
