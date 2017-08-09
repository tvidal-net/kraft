package net.tvidal.kraft.storage

interface KRaftLog<out T : KRaftEntry> {

    val lastLogIndex: Long

    val lastLogTerm: Long

    fun append(entries: KRaftEntryBatch<KRaftEntry>): Long

    fun read(): List<T>

}
