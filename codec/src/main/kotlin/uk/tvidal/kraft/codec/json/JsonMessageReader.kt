package uk.tvidal.kraft.codec.json

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.JsonElement
import com.google.gson.stream.JsonReader
import uk.tvidal.kraft.message.Message
import java.io.Reader

class JsonMessageReader(reader: Reader) : Iterable<Message?> {

    private val reader: JsonReader = gson.newJsonReader(reader)

    override fun iterator() = object : Iterator<Message?> {

        override fun hasNext(): Boolean = true

        override fun next(): Message? {
            val json = gson.fromJson<JsonElement>(reader)
            val envelope = gson.fromJson<JsonMessageEnvelope>(json)
            val name = envelope.type
            val type = MessageCodec[name]
            return if (type == null) null
            else gson.fromJson<Message>(json, type.java)
        }
    }
}
