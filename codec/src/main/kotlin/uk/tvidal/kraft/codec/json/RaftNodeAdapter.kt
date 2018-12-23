package uk.tvidal.kraft.codec.json

import com.google.gson.JsonParseException
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.client.toInt
import java.net.InetAddress

class RaftNodeAdapter : TypeAdapter<RaftNode>() {

    companion object {
        const val CLIENT_NODE = "Client"
        const val RAFT_NODE = "Raft"

        val regex = Regex("(\\w+)\\((\\w+):([0-9.]+)\\)")
    }

    override fun write(writer: JsonWriter, node: RaftNode) {
        val type = if (node.clientNode) CLIENT_NODE else RAFT_NODE
        writer.value("$type($node)")
    }

    override fun read(reader: JsonReader): RaftNode {
        val value = reader.nextString()
        val match = regex.matchEntire(value)?.groupValues
            ?: throw JsonParseException("Cannot read '$value' as a RaftNode")

        val clientNode = match[1] == CLIENT_NODE
        val cluster = match[2]
        val index = match[3].let {
            if (clientNode) clientNodeIndex(it)
            else it.toInt()
        }

        return RaftNode(index, cluster, clientNode)
    }

    private fun clientNodeIndex(s: String): Int = InetAddress
        .getByName(s)
        .toInt()
}
