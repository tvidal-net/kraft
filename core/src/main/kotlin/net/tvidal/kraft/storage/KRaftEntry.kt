package net.tvidal.kraft.storage

class KRaftEntry internal constructor(
    val term: Long,
    val payload: ByteArray
) {
    val bytes: Int
        get() = payload.size

    operator fun component1() = term
    operator fun component2() = payload
    operator fun component3() = bytes

    override fun toString() = "Entry[term=$term bytes=$bytes]"
}
