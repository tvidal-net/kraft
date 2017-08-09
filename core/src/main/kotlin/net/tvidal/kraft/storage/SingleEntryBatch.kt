package net.tvidal.kraft.storage

class SingleEntryBatch<out T : RaftEntry>(singleEntry: T) : RaftEntryBatch<RaftEntry> {

    override val entries: List<T> = listOf(singleEntry)

    companion object {
        fun empty(term: Long) = SingleEntryBatch(EmptyEntry(term))
    }

}
