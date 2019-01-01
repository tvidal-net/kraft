package uk.tvidal.kraft.codec.json.adapter

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.codec.json.nullable

class RaftNodeAdapter : TypeAdapter<RaftNode>() {

    override fun write(writer: JsonWriter, node: RaftNode?) {
        if (node != null) writer.value(node.name)
        else writer.nullValue()
    }

    override fun read(reader: JsonReader): RaftNode? = reader.nullable {
        RaftNode.parseFrom(nextString())
    }
}
