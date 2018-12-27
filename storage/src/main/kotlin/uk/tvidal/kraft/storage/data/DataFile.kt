package uk.tvidal.kraft.storage.data

import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.WRITABLE
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.COMMITTED

interface DataFile {

    val state: FileState

    val immutable: Boolean
        get() = state != WRITABLE

    val committed: Boolean
        get() = state == COMMITTED

    fun truncateAt(index: Long)

    fun release()
}
