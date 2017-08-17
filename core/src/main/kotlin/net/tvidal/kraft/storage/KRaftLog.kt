package net.tvidal.kraft.storage

interface KRaftLog {

    val firstLogIndex: Long
    val lastLogIndex: Long
    val lastLogTerm: Long

    fun append(entries: Iterable<KRaftEntry>, fromIndex: Long = nextLogIndex): Long

    fun read(fromIndex: Long, byteLimit: Int): KRaftEntryBatch

    // should be 0 for index 0
    fun termAt(index: Long): Long
}
