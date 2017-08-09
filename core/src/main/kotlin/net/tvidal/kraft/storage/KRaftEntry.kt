package net.tvidal.kraft.storage

import net.tvidal.kraft.INT_BYTES
import net.tvidal.kraft.LONG_BYTES

interface KRaftEntry {

    val bytes: Int

    val term: Long

    val checksum: Int

    companion object {
        const val EMPTY_BYTES = LONG_BYTES + INT_BYTES
    }

}
