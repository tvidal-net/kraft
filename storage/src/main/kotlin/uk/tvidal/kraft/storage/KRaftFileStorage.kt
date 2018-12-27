package uk.tvidal.kraft.storage

import uk.tvidal.kraft.FIRST_INDEX
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.CLOSED
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.DISCARDED
import uk.tvidal.kraft.logging.KRaftLogging
import uk.tvidal.kraft.storage.config.FileFactory
import java.util.TreeMap

class KRaftFileStorage(
    val config: FileFactory
) : KRaftStorage {

    internal companion object : KRaftLogging()

    internal val files = TreeMap<LongRange, KRaftFile>(longRangeComparator)

    override var firstLogIndex: Long = FIRST_INDEX
        private set

    override var lastLogIndex: Long = 0L
        private set

    override var lastLogTerm: Long = 0L
        get() = termAt(lastLogIndex)

    internal var lastFileIndex: Int
        private set

    internal var currentFile: KRaftFile
        private set

    init {
        log.info { "starting config=$config" }
        files.putAll(config.open())
        lastFileIndex = files.values
            .map(KRaftFile::index)
            .max() ?: 0

        files.entries
            .map { it.value }
            .filter { it.state == DISCARDED }
            .forEach { remove(it) }

        if (files.isNotEmpty()) {
            currentFile = files[files.lastKey()]!!
            firstLogIndex = files.firstKey().first
            lastLogIndex = currentFile.lastIndex

            // file range can still change,
            // so it should be removed from the list
            if (currentFile.immutable) createNewFile()
            else files.remove(currentFile.range)
        } else {
            // Empty directory, create the first file
            currentFile = config.create(FIRST_INDEX, ++lastFileIndex)
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
                createNewFile()
            }
            toAppend -= appended
        }
        return lastLogIndex
    }

    private fun truncateAt(index: Long) {
        if (index > nextLogIndex) {
            throw IllegalStateException("Cannot truncate after the current file")
        }
        while (index < currentFile.firstIndex) {
            val prev = currentFile.prev
            discard()
            if (prev == null) {
                if (index > FIRST_INDEX) {
                    throw IllegalStateException("Cannot truncate behind the first file!")
                }
                lastLogIndex = 0L
                createNewFile()
                return
            }
            currentFile = prev
        }
        if (index in currentFile) {
            currentFile.truncateAt(index)
            lastLogIndex = index - 1
            createNewFile()
        }
    }

    private fun createNewFile() {
        if (!currentFile.immutable)
            currentFile.close(CLOSED)

        val range = currentFile.range
        files[range] = currentFile

        val prev = currentFile
        currentFile = config.create(
            firstIndex = nextLogIndex,
            fileIndex = ++lastFileIndex
        )
        currentFile.prev = prev
        prev.next = currentFile
        log.info { "created file $currentFile" }
    }

    private fun discard() {
        currentFile.close(DISCARDED)
        remove(currentFile)
        log.info { "discarded file $currentFile" }
    }

    private fun remove(file: KRaftFile) {
        file.removeFromChain()
        files.remove(file.range)
        file.release()
        log.info { "removed file $file" }
    }

    override fun toString() = "($firstLogIndex..$lastLogIndex) file=$currentFile"
}
