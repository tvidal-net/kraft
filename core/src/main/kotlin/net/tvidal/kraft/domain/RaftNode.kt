package net.tvidal.kraft.domain

data class RaftNode(

  val clusterName: String,
  val nodeIndex: Byte

) {

    override fun toString(): String {
        return "$clusterName:$nodeIndex"
    }

}
