package uk.tvidal.kraft.storage

import uk.tvidal.kraft.FIRST_INDEX
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.DISCARDED
import uk.tvidal.kraft.codec.binary.BinaryCodec.FileState.TRUNCATED
import uk.tvidal.kraft.createLinks
import uk.tvidal.kraft.logging.KRaftLogging
import uk.tvidal.kraft.storage.config.FileFactory
import java.io.Closeable
import java.util.TreeMap

class KRaftFileStorage(
    val factory: FileFactory
) : KRaftStorage,
    MutableIndexRange {

    internal companion object : KRaftLogging()

    internal val files = TreeMap<LongRange, KRaftFile>(longRangeComparator)

    override var range: LongRange = LongRange.EMPTY

    override val firstLogIndex: Long
        get() = range.first

    override val lastLogIndex: Long
        get() = range.last

    override var lastLogTerm: Long = 0L
        get() = termAt(lastLogIndex)

    internal var lastFileIndex: Int
        private set

    internal var currentFile: KRaftFile
        private set

    init {
        log.info { "starting config=$factory" }
        val allFiles = factory.open()
        lastFileIndex = allFiles
            .map(KRaftFile::index)
            .max() ?: 0

        // remove discarded
        files.putAll(
            allFiles.filter {
                if (it.discarded) {
                    it.release()
                    false
                } else true
            }.associateBy(KRaftFile::range)
        )
        validateFileSequence()
        createLinks(files.values)

        if (files.isNotEmpty()) {
            currentFile = files[files.lastKey()]!!
            range = LongRange(files.firstKey().first, currentFile.lastIndex)

            // file range can still change,
            // so it should be removed from the list
            if (currentFile.immutable) createNewFile()
            else files.remove(currentFile.range)
        } else {
            // Empty directory, create the first file
            currentFile = factory.create(FIRST_INDEX, ++lastFileIndex)
        }
    }

    private fun validateFileSequence() {
        val files = files.values.toList()
        for (i in 1 until files.size) {
            val prev = files[i - 1]
            val file = files[i]
            if (prev.lastIndex != file.firstIndex - 1) {
                throw FileSequenceGapException("There is an index gap between [$prev] -> [$file]")
            }
        }
    }

    override fun commit(commitIndex: Long) {
    }

    override fun read(fromIndex: Long, byteLimit: Int): KRaftEntries {
        if (fromIndex == nextLogIndex) {
            return emptyEntries()
        }
        var entries = emptyEntries()
        var bytes = byteLimit
        var index = fromIndex
        var file: KRaftFile? = fileSearch(fromIndex)
        while (file != null) {
            entries += file.read(index, bytes)
            index += entries.size
            bytes -= entries.bytes
            file = if (entries.isEmpty || index in file) null
            else file.next
        }
        return entries
    }

    override fun termAt(index: Long): Long = when (index) {
        in range -> {
            val file = fileSearch(index)
            val entry = file[index]
            val term = entry.term
            log.trace { "termAt index=$index term=$term file=$file" }
            term
        }
        else -> 0L
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
        throw IndexOutOfRangeException("Could not find file containing entry: $index")
    }

    override fun append(entries: KRaftEntries, fromIndex: Long): Long {
        truncateAt(fromIndex)
        var toAppend = entries
        while (!toAppend.isEmpty) {
            val appended = currentFile.append(toAppend)
            lastIndex += appended

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
            throw TruncateOutOfRangeException("Cannot truncate after the current file")
        }
        while (index < currentFile.firstIndex) {
            val prev = currentFile.prev
            discard()
            if (prev == null) {
                if (index > FIRST_INDEX) {
                    throw TruncateOutOfRangeException("Cannot truncate behind the first file!")
                }
                lastIndex = 0L
                createNewFile()
                return
            }
            files.remove(prev.range)
            currentFile = prev
        }
        if (index in currentFile) {
            if (index > currentFile.firstIndex) {
                files.remove(currentFile.range)
                currentFile.truncateAt(index)
            } else discard()

            lastIndex = index - 1
            createNewFile()
        }
    }

    private fun createNewFile() {
        if (!currentFile.immutable)
            currentFile.close(TRUNCATED)

        if (!currentFile.discarded) {
            val range = currentFile.range
            files[range] = currentFile
        }

        val prev = currentFile
        currentFile = factory.create(
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

    override fun close() {
        files.values.forEach(Closeable::close)
        currentFile.close()
        files.clear()
    }

    override fun toString() = "($firstLogIndex..$lastLogIndex) file=$currentFile"
}
