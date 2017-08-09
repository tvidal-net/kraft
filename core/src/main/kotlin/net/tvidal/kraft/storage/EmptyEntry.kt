package net.tvidal.kraft.storage

import net.tvidal.kraft.storage.KRaftEntry.Companion.EMPTY_BYTES

class EmptyEntry(override val term: Long) : KRaftEntry {

    override val bytes = EMPTY_BYTES

    override val checksum = 0

}
