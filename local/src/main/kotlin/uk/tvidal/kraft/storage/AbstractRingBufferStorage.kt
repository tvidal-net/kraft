package uk.tvidal.kraft.storage

abstract class AbstractRingBufferStorage(protected val size: Int) : KRaftStorage {

    private val data = Array(size) { emptyEntry() }

    final override val firstLogIndex: Long
        get() = maxOf(lastLogIndex - size, 0) + 1

    final override var lastLogIndex: Long = 0L
        private set

    final override var lastLogTerm: Long = 0L
        private set

    final override fun termAt(index: Long) = this[index].term

    protected fun read(range: LongRange) = when {
        range.isEmpty() -> emptyEntries()
        range.first == range.last -> singleEntry(this[range.first])
        else -> KRaftEntries(range.map { this[it] })
    }

    protected operator fun get(index: Long) = when (index) {
        0L -> emptyEntry()
        nextLogIndex -> data[pos(Long.MAX_VALUE)] // force exception
        else -> data[pos(index)]
    }

    protected fun truncateBefore(index: Long) {
        pos(index)
        lastLogIndex = index - 1
    }

    protected fun append(entry: KRaftEntry) {
        val index = pos(++lastLogIndex)
        data[index] = entry
        lastLogTerm = entry.term
    }

    private fun pos(index: Long) = when {
        index <= 0L || index < firstLogIndex -> {
            throw IllegalArgumentException("Attempt to access before the start of the log (index=$index firstLogIndex=$firstLogIndex)")
        }
        index > nextLogIndex -> {
            throw IllegalArgumentException("Attempt to access past the end of the log (index=$index lastLogIndex=$lastLogIndex)")
        }
        else -> (index % size).toInt()
    }

    override fun toString() = "[($firstLogIndex->$lastLogIndex) nextLogIndex=$nextLogIndex]"
}
