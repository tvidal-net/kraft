package net.tvidal.kraft.storage

interface KRaftEntryBatch<out T : KRaftEntry> {

    val entries: List<T>

}
