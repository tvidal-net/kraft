package uk.tvidal.kraft

data class RaftNode(
    val index: Int,
    val cluster: String = DEFAULT_CLUSTER_NAME,
    val clientNode: Boolean = false
) {
    companion object {
        val EMPTY = RaftNode(0, "")
        private const val BYTE_MASK = 0xFF
    }

    private val name = buildString {
        append(cluster)
        append(':')
        if (clientNode) {
            repeat(3) {
                val bits = (3 - it) * 8
                append(index shr bits and BYTE_MASK)
                append('.')
            }
            append(index and BYTE_MASK)
        } else append(index)
    }

    init {
        if (cluster.length > MAX_CLUSTER_NAME_LENGTH) {
            throw IllegalArgumentException("The cluster name cannot exceed $MAX_CLUSTER_NAME_LENGTH characters")
        }
    }

    override fun toString() = name
}
