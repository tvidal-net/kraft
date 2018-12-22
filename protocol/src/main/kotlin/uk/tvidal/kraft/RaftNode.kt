package uk.tvidal.kraft

data class RaftNode(
    val index: Byte,
    val cluster: String = DEFAULT_CLUSTER_NAME
) {
    companion object {
        val EMPTY = RaftNode(0, "")
    }

    init {
        if (cluster.length > MAX_CLUSTER_NAME_LENGTH) {
            throw IllegalArgumentException("The cluster name cannot exceed $MAX_CLUSTER_NAME_LENGTH characters")
        }
    }

    override fun toString() = "$cluster:$index"
}
