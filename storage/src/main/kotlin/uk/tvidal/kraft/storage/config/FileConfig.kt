package uk.tvidal.kraft.storage.config

import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.DISCARDED
import uk.tvidal.kraft.storage.data.KRaftData
import uk.tvidal.kraft.storage.index.KRaftIndex
import java.io.File
import java.nio.file.Path

class FileConfig(
    name: FileName,
    val path: Path,
    val size: Long,
    val firstIndex: Long
) {

    var name: FileName = name
        private set

    val file: File
        get() = name.current(path)

    val data: KRaftData = file.let {
        if (it.exists()) KRaftData.open(file)
        else KRaftData.create(file, size, firstIndex)
    }

    val index = KRaftIndex(name.index(path))

    init {
        if (index.isEmpty() && !data.isEmpty()) {
            index.append(data.rebuildIndex())
            if (data.immutable) index.close()
        }
    }

    fun close(state: FileState) {
        if (state == DISCARDED && data.committed)
            throw IllegalStateException("Cannot discard a committed file!")

        index.close()
        data.close(state)

        name = name.copy(state = state)
        file.renameTo(path.resolve(name.current).toFile())
    }
}
