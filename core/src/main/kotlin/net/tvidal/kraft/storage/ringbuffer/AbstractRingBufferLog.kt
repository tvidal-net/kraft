package net.tvidal.kraft.storage.ringbuffer

import net.tvidal.kraft.storage.KRaftEntries
import net.tvidal.kraft.storage.KRaftEntry
import net.tvidal.kraft.storage.KRaftLog
import net.tvidal.kraft.storage.emptyEntries
import net.tvidal.kraft.storage.emptyEntry
import net.tvidal.kraft.storage.nextLogIndex
import net.tvidal.kraft.storage.singleEntry

abstract class AbstractRingBufferLog(val size: Int) : KRaftLog {

    private val data = Array(size) { emptyEntry() }

    final override val firstLogIndex get() = maxOf(lastLogIndex - size, 1L)
    final override var lastLogIndex = 0L; private set
    final override var lastLogTerm = 0L; private set

    final override fun termAt(index: Long) = this[index].term

    protected fun read(range: LongRange) = when {
        range.isEmpty() -> emptyEntries()
        range.first == range.last -> singleEntry(this[range.first])
        else -> KRaftEntries(range.map { this[it] })
    }

    protected operator fun get(index: Long) = when (index) {
        0L -> emptyEntry()
        nextLogIndex -> data[pos(Long.MAX_VALUE)] // force exception
        else -> {
            val pos = pos(index)
            data[pos]
        }
    }

    protected operator fun set(index: Long, value: KRaftEntry) {
        val pos = pos(index)
        data[pos] = value
        lastLogIndex = index
        lastLogTerm = value.term
    }

    private fun pos(index: Long) = when {
        index <= 0L || index < firstLogIndex -> {
            throw IllegalArgumentException("Attempt to access before the start of the log")
        }
        index > nextLogIndex -> {
            throw IllegalArgumentException("Attempt to access past the end of the log")
        }
        else -> (index % data.size).toInt()
    }

    override fun toString() = "[($firstLogIndex:$lastLogIndex) nextLogIndex=$nextLogIndex size=$size"
}
