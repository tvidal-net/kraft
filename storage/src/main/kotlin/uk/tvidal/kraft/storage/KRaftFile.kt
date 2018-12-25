package uk.tvidal.kraft.storage

import java.nio.file.Path

class KRaftFile(
    val file: Path,
    val fileSize: Long
) {
    private val indexFile = KRaftIndexFile(file.toFile())
    private val dataFile = KRaftDataFile(file, fileSize, indexFile::append)

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
