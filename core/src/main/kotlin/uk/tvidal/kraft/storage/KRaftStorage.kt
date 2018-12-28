package uk.tvidal.kraft.storage

import java.io.Closeable

interface KRaftStorage : Closeable {

    val firstLogIndex: Long
    val lastLogIndex: Long
    val lastLogTerm: Long

    val nextLogIndex: Long
        get() = lastLogIndex + 1

    fun append(entries: KRaftEntries, fromIndex: Long = nextLogIndex): Long

    fun read(fromIndex: Long, byteLimit: Int): KRaftEntries

    // should be 0 for index 0
    fun termAt(index: Long): Long

    fun commit(commitIndex: Long)
}
