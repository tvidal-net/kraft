package net.tvidal.kraft.storage

interface KRaftLog {

    val firstLogIndex: Long
    val lastLogIndex: Long
    val lastLogTerm: Long

    val nextLogIndex: Long
        get() = lastLogIndex + 1

    fun append(entries: Iterable<KRaftEntry>, fromIndex: Long = nextLogIndex): Long

    fun read(fromIndex: Long, byteLimit: Int): KRaftEntries

    // should be 0 for index 0
    fun termAt(index: Long): Long
}
