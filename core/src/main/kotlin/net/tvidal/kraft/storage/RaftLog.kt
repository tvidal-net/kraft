package net.tvidal.kraft.storage

interface RaftLog<out T : RaftEntry> {

    val lastLogIndex: Long

    val lastLogTerm: Long

    fun append(entries: RaftEntryBatch<RaftEntry>): Long

    fun read(): List<T>

}
