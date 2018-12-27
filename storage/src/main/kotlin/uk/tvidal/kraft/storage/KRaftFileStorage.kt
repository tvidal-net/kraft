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

    internal val files = TreeMap<LongRange, KRaftFile>(longRangeComparator)
        .apply { putAll(config.listFiles()) }

    internal lateinit var currentFile: KRaftFile
        private set

    override var firstLogIndex: Long = FIRST_INDEX
        private set

    override var lastLogIndex: Long = 0L
        private set

    override var lastLogTerm: Long = 0L
        get() = termAt(lastLogIndex)

    init {
        log.info { "starting config=$config" }
        if (files.isNotEmpty()) {
            val lastFile = files.lastEntry()
            firstLogIndex = files.firstKey().first
            lastLogIndex = lastFile.key.last

            if (lastFile.value.immutable) {
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

    override fun append(entries: KRaftEntries, fromIndex: Long): Long {
        truncateAt(fromIndex)
        var toAppend = entries
        while (!toAppend.isEmpty) {
            val appended = currentFile.append(toAppend)
            lastLogIndex += appended

            if (appended < toAppend.size) {
                // file is full, rotate
                createNewFile(lastLogIndex + 1)
            }
            toAppend -= appended
        }
        return lastLogIndex
    }

    private fun truncateAt(index: Long) {
        if (index > nextLogIndex)
            throw IllegalStateException("Cannot truncate after the current file")

        while (index < currentFile.firstIndex) {
            val prev = currentFile.prev
            if (prev == null) {
                if (index > FIRST_INDEX)
                    throw IllegalStateException("Cannot truncate behind the first file!")

                createNewFile(index)
                return
            }
            discard(currentFile)
            currentFile = prev
        }
        if (index in currentFile) {
            currentFile.truncateAt(index)
            createNewFile(index)
        }
    }

    private fun commit() {
    }

    private fun discard(file: KRaftFile) {
        remove(file)
        file.close(DISCARDED)
    }

    private fun remove(file: KRaftFile) {
        file.removeFromChain()
        files.remove(file.range)
        log.info { "removed file $file" }
    }

    private fun createNewFile(firstIndex: Long, fileName: FileName = currentFile.nextFileName) {
        if (firstIndex > FIRST_INDEX) {
            val range = currentFile.range
            files[range] = currentFile
        }
        currentFile = config.createFile(
            name = fileName,
            firstIndex = firstIndex
        )
        log.info { "created file $currentFile" }
    }

    override fun toString() = "($firstLogIndex..$lastLogIndex) file=$currentFile"
}
