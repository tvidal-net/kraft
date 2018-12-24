package uk.tvidal.kraft.codec.json.adapter

import com.google.gson.JsonParseException
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.client.toInt
import uk.tvidal.kraft.codec.json.nullable
import java.net.InetAddress

class RaftNodeAdapter : TypeAdapter<RaftNode>() {

    companion object {
        const val CLIENT_NODE = "Client"
        const val RAFT_NODE = "Raft"

        val regex = Regex("(\\w+)\\((\\w+):([0-9.]+)\\)")
    }

    override fun write(writer: JsonWriter, node: RaftNode?) {
        if (node != null) {
            val type = if (node.clientNode) CLIENT_NODE else RAFT_NODE
            writer.value("$type($node)")
        } else writer.nullValue()
    }

    override fun read(reader: JsonReader): RaftNode? = reader.nullable {
        val value = nextString()
        val match = regex.matchEntire(value)?.groupValues
            ?: throw JsonParseException("Cannot read '$value' as a RaftNode")

        val clientNode = match[1] == CLIENT_NODE
        val cluster = match[2]
        val index = match[3].let { index ->
            if (clientNode) clientNodeIndex(index)
            else index.toInt()
        }
        return RaftNode(index, cluster, clientNode)
    }

    private fun clientNodeIndex(s: String): Int = InetAddress
        .getByName(s)
        .toInt()
}
