package uk.tvidal.kraft.storage

class RingBufferStorage(size: Int = 4096) : AbstractRingBufferStorage(size) {

    override fun append(entries: KRaftEntries, fromIndex: Long): Long {
        truncateBefore(fromIndex)
        entries.forEach(this::append)
        return lastLogIndex
    }

    override fun read(fromIndex: Long, byteLimit: Int): KRaftEntries {
        var size = 0
        var index = fromIndex
        while (index <= lastLogIndex) {
            val entry = this[index]
            val bytes = entry.bytes
            if (size + bytes <= byteLimit) {
                size += bytes
                index += 1
            } else break
        }
        return read(fromIndex until index)
    }
}
