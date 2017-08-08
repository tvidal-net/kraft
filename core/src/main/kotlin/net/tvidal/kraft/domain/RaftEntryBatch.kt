package net.tvidal.kraft.domain

interface RaftEntryBatch<out T : RaftEntry> {

    val entries: List<T>

}
