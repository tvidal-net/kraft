package uk.tvidal.kraft

data class RaftNode(
    val index: Int,
    val cluster: String = DEFAULT_CLUSTER_NAME,
    val clientNode: Boolean = false
) {
    companion object {
        val EMPTY = RaftNode(0, "EMPTY")
        private const val BYTE_MASK = 0xFF

        private const val colon = ':'
        private const val dot = '.'

        fun parseFrom(text: String): RaftNode {
            try {
                val values = text.split(colon)
                val (cluster, index) = values
                val clientNode = dot in index
                val parsedIndex = parseIndex(index, clientNode)
                return RaftNode(parsedIndex, cluster, clientNode)
            } catch (e: Exception) {
                throw IllegalArgumentException("Cannot parse a RaftNode from '$text'", e)
            }
        }

        private fun parseIndex(index: String, clientNode: Boolean): Int = when {
            clientNode -> index.split(dot).foldIndexed(0) { i, acc, s ->
                val bits = (3 - i) * 8
                s.toInt() shl bits or acc
            }
            else -> index.toInt()
        }

        private fun StringBuilder.formatAddress(index: Int) {
            repeat(3) {
                val bits = (3 - it) * 8
                append(index shr bits and BYTE_MASK)
                append(dot)
            }
            append(index and BYTE_MASK)
        }
    }

    val name = buildString {
        append(cluster)
        append(colon)
        if (clientNode) formatAddress(index)
        else append(index)
    }

    init {
        if (cluster.length > MAX_CLUSTER_NAME_LENGTH) {
            throw IllegalArgumentException("The cluster name cannot exceed $MAX_CLUSTER_NAME_LENGTH characters")
        }
    }

    override fun toString() = name
}
