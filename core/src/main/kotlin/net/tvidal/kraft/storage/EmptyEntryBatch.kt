package net.tvidal.kraft.storage

class EmptyEntryBatch() : KRaftEntryBatch {

    override val bytes = 0

    override val entries = listOf<KRaftEntry>()

}
