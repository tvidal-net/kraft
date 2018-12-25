package uk.tvidal.kraft.storage

import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.ACTIVE
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.COMMITTED

interface KRaftFileState {

    val state: FileState

    val immutable: Boolean
        get() = state != ACTIVE

    val committed: Boolean
        get() = state == COMMITTED

    fun truncateAt(index: Long)
}
