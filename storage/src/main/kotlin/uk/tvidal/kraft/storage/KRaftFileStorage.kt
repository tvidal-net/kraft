package uk.tvidal.kraft.storage

import uk.tvidal.kraft.FIRST_INDEX
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.DISCARDED
import uk.tvidal.kraft.logging.KRaftLogging
import uk.tvidal.kraft.storage.config.FileName
import uk.tvidal.kraft.storage.config.FileStorageConfig
import java.util.TreeMap

class KRaftFileStorage(
    val config: FileStorageConfig
) : KRaftStorage {

    internal companion object : KRaftLogging()

    internal val files = TreeMap<LongRange, KRaftFile>(
        config.listFiles()
            .associateBy(KRaftFile::range)
    )

    internal lateinit var currentFile: KRaftFile

    override var firstLogIndex: Long = FIRST_INDEX
        private set

    override var lastLogIndex: Long = 0L
        private set

    override var lastLogTerm: Long = 0L
        get() = termAt(lastLogIndex)

    init {
        if (files.isNotEmpty()) {
            val lastFile = files.lastEntry()
            firstLogIndex = files.firstKey().first
            lastLogIndex = lastFile.key.last

            if (lastFile.value.dataFile.immutable) {
                createNewFile(nextLogIndex)
                currentFile.prev = lastFile.value
            } else {
                files.remove(lastFile.key)
                currentFile = lastFile.value
            }
        } else {
            createNewFile(firstLogIndex, config.firstFileName)
        }
    }

    override fun commit(commitIndex: Long) {
    }

    override fun append(entries: KRaftEntries, fromIndex: Long): Long {
        truncateAt(fromIndex)
        var index = fromIndex
        var toAppend = entries
        while (!toAppend.isEmpty) {
            val appended = currentFile.append(toAppend)
            toAppend -= appended
            index += appended
        }
        lastLogIndex = index
        assert(lastLogIndex == currentFile.range.last)
        return lastLogIndex
    }

    override fun read(fromIndex: Long, byteLimit: Int): KRaftEntries {
        assert(fromIndex in firstLogIndex..lastLogIndex)
        var entries = emptyEntries()
        var bytes = byteLimit
        var index = fromIndex
        var file: KRaftFile? = fileSearch(fromIndex)
        while (file != null && bytes > 0) {
            entries += file.read(index, bytes)
            if (entries.isEmpty) break // next entry can't fit
            index += entries.size
            bytes -= entries.bytes
            file = file.next
        }
        return entries
    }

    override fun termAt(index: Long): Long {
        val file = fileSearch(index)
        val entry = file[index]
        return entry.term
    }

    private fun fileSearch(index: Long): KRaftFile {
        val prev = currentFile.prev
        if (index in currentFile) return currentFile
        else if (prev != null && index in prev) return prev

        val keys = files.keys.toList()
        var low = 0
        var high = keys.size - 1
        while (low <= high) {
            val mid = (low + high) / 2
            val range = keys[mid]
            when {
                index > range.last -> low = mid + 1
                index < range.first -> high = mid - 1
                else -> return files[range]!!
            }
        }
        throw IllegalArgumentException("Could not find file containing entry: $index")
    }

    private fun truncateAt(index: Long) {
        if (index > nextLogIndex)
            throw IllegalStateException("Cannot truncate after he current file!")

        while (index < firstLogIndex) {
            discard(currentFile)
            val prev = currentFile.prev
            if (prev == null) {
                if (index > FIRST_INDEX)
                    throw IllegalStateException("Cannot truncate behind the first file!")

                createNewFile(index)
                return
            }
            currentFile = prev
        }
        if (index in currentFile) {
            currentFile.dataFile.truncateAt(index)
            createNewFile(index)
        }
    }

    private fun discard(file: KRaftFile) {
        file.close(DISCARDED)
        val next = file.next
        if (next != null) next.prev = file.prev
        val prev = (file.prev)
        if (prev != null) prev.next = file.next
        files.remove(file.range)
    }

    private fun createNewFile(firstIndex: Long, fileName: FileName = currentFile.fileName.next) {
        if (firstIndex > FIRST_INDEX) {
            val range = currentFile.range
            files[range] = currentFile
        }
        currentFile = config.createFile(
            name = fileName,
            firstIndex = firstIndex
        )
    }
}
