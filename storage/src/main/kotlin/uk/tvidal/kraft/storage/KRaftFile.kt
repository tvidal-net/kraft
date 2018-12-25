package uk.tvidal.kraft.storage

import java.io.File

class KRaftFile(
    val file: File,
    val fileSize: Long
) {
    private val indexFile = KRaftIndexFile(file)
    private val dataFile = KRaftDataFile(file, fileSize, 0L, indexFile::append)

    operator fun contains(index: Long) = index in indexFile.range

    fun read(fromIndex: Long, byteLimit: Int): List<KRaftEntry> = indexFile
        .read(fromIndex, byteLimit)
        .map { dataFile[it] }

    fun append(fromIndex: Long, entries: Iterable<KRaftEntry>): Long {
        var index = fromIndex
        for (e in entries) {
            val i = dataFile.append(index++, e)
            indexFile.append(i)
        }
        return index
    }
}
