package uk.tvidal.kraft

import uk.tvidal.kraft.domain.RaftCluster
import uk.tvidal.kraft.domain.RaftNode

const val DEFAULT_CLUSTER_NAME = "KRAFT"
const val FOREVER = 253402300799999L // 9999-12-31 23:59:59.999
const val NEVER = -1L
const val NOW = 0L

const val BEFORE_LOG = 0L

const val LONG_BYTES = java.lang.Long.BYTES
const val INT_BYTES = java.lang.Integer.BYTES
const val SHORT_BYTES = java.lang.Short.BYTES
const val BYTE_BYTES = java.lang.Byte.BYTES

fun raftNodes(size: Int, clusterName: String = DEFAULT_CLUSTER_NAME) =
    (1..size).map { RaftNode(it.toByte(), clusterName) }

fun raftCluster(size: Int, clusterName: String = DEFAULT_CLUSTER_NAME) =
    raftNodes(size, clusterName).let { nodes -> (1..size).map { RaftCluster(it, nodes) } }

