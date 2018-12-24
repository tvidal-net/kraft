package uk.tvidal.kraft.storage

class KRaftEntries(
    private val data: Collection<KRaftEntry>
) : Iterable<KRaftEntry> {

    val bytes = data.sumBy { it.bytes }

    val size: Int
        get() = data.size

    val isEmpty: Boolean
        get() = data.isEmpty()

    fun copy(newTerm: Long): KRaftEntries = KRaftEntries(
        data = data.map { it.copy(newTerm) }
    )

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
