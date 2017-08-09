package net.tvidal.kraft.storage

class SingleEntryBatch(singleEntry: KRaftEntry) : KRaftEntryBatch {

    override val entries = listOf(singleEntry)

    override val bytes = singleEntry.bytes

    companion object {
        fun empty(term: Long) = SingleEntryBatch(EmptyEntry(term))
    }

}
