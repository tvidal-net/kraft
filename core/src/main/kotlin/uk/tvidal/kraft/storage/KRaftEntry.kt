package uk.tvidal.kraft.storage

import java.util.Arrays

class KRaftEntry internal constructor(
    val payload: ByteArray,
    val term: Long
) {
    val bytes: Int
        get() = payload.size

    val isEmpty: Boolean
        get() = payload.isEmpty()

    operator fun component1() = term
    operator fun component2() = payload
    operator fun component3() = bytes

    fun toEntries() = singleEntry(this)

    fun copy(newTerm: Long) = KRaftEntry(payload, newTerm)

    override fun hashCode() = term.hashCode() xor Arrays.hashCode(payload)

    override fun equals(other: Any?) = when {
        this === other -> true
        other !is KRaftEntry -> false
        term != other.term -> false
        else -> Arrays.equals(payload, other.payload)
    }

    override fun toString() = "Entry[term=$term bytes=$bytes]"
}
