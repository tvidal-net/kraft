package net.tvidal.kraft.storage

interface RaftEntryBatch<out T : RaftEntry> {

    val entries: List<T>

}
