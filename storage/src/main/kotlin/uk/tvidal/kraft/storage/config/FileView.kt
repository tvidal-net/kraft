package uk.tvidal.kraft.storage.config

import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState
import uk.tvidal.kraft.storage.data.KRaftData
import uk.tvidal.kraft.storage.index.KRaftIndex

interface FileView {
    val name: FileName
    val data: KRaftData
    val index: KRaftIndex
    fun rename(state: FileState)
}
