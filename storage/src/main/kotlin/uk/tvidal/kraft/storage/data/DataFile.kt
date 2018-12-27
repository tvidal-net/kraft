package uk.tvidal.kraft.storage.data

import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.COMMITTED
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.DISCARDED
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.WRITABLE

interface DataFile {

    val state: FileState

    val immutable: Boolean
        get() = state != WRITABLE

    val committed: Boolean
        get() = state == COMMITTED

    val discarded: Boolean
        get() = state == DISCARDED

    fun truncateAt(index: Long)

    fun release()
}
