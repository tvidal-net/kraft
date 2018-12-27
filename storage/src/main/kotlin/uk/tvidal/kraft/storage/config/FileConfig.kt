package uk.tvidal.kraft.storage.config

import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.DISCARDED
import uk.tvidal.kraft.storage.data.KRaftData
import uk.tvidal.kraft.storage.index.KRaftIndex
import java.io.File
import java.nio.file.Path

internal class FileConfig(
    name: FileName,
    val firstIndex: Long,
    val fileLength: Long,
    val path: Path
) {
    internal var name: FileName = name
        private set

    internal val file: File
        get() = name.current(path)

    internal val data: KRaftData = file.let {
        if (it.exists()) KRaftData.open(file)
        else KRaftData.create(file, fileLength, firstIndex)
    }

    internal val index = KRaftIndex(name.index(path))

    init {
        if (index.isEmpty() && !data.isEmpty()) {
            index.append(data.rebuildIndex())
            if (data.immutable) index.close()
        }
    }

    internal fun close(state: FileState) {
        if (state == DISCARDED && data.committed) {
            throw IllegalStateException("Cannot discard a committed file!")
        }
        index.close()
        data.close(state)
        name = name.rename(state, path)
    }

    override fun toString() = "$name[fromIndex=$firstIndex size=$fileLength]"
}
