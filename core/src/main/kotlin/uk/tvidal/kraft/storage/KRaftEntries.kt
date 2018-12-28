package uk.tvidal.kraft.storage

import java.util.UUID

class KRaftEntries(
    private val data: Collection<KRaftEntry>
) : Iterable<KRaftEntry> {

    val bytes = data.sumBy { it.bytes }

    val size: Int
        get() = data.size

    val isEmpty: Boolean
        get() = data.isEmpty()

    val id: UUID?
        get() = data.firstOrNull()?.id

    fun copy(newTerm: Long): KRaftEntries = KRaftEntries(
        data = data.map { it.copy(newTerm) }
    )

    operator fun plus(other: KRaftEntries) = KRaftEntries(data + other.data)

    operator fun minus(count: Int) =
        if (count < data.size) KRaftEntries(data.drop(count))
        else emptyEntries()

    override fun iterator() = data.iterator()

    override fun hashCode() = data.hashCode()

    override fun equals(other: Any?) = when {
        other === this -> true
        other !is KRaftEntries -> false
        other.size != size -> false
        other.bytes != bytes -> false
        else -> data == other.data
    }

    override fun toString() = "Entries[size=$size bytes=$bytes]"
}
