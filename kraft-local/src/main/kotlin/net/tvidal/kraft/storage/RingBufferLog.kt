package net.tvidal.kraft.storage

class RingBufferLog(size: Int) : AbstractRingBufferLog(size) {

    override fun append(entries: Iterable<KRaftEntry>, fromIndex: Long): Long {
        for ((i, entry) in entries.withIndex()) {
            this[fromIndex + i] = entry
        }
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
