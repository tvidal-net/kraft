package net.tvidal.kraft.storage

interface KRaftEntryBatch {

    val entries: List<KRaftEntry>

    val bytes: Int

}
