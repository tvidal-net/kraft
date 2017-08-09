package net.tvidal.kraft.storage

interface KRaftLog<out T : KRaftEntry> {

    val lastLogTerm: Long

    val lastLogIndex: Long

    val nextLogIndex; get() = lastLogIndex + 1

    fun append(entries: KRaftEntryBatch, fromIndex: Long = nextLogIndex): Long

    fun read(fromIndex: Long, sizeLimit: Int): KRaftEntryBatch

    // should be 0 for index 0
    fun termAt(index: Long): Long?

}
