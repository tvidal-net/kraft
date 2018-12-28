package uk.tvidal.kraft.codec.json

import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.get
import com.google.gson.JsonElement
import com.google.gson.stream.JsonReader
import uk.tvidal.kraft.codec.MessageCodec
import uk.tvidal.kraft.message.Message
import java.io.Reader

class JsonMessageReader(reader: Reader) : Iterable<Message?> {

    private val reader: JsonReader = gson.newJsonReader(reader)

    override fun iterator() = object : Iterator<Message?> {

        override fun hasNext(): Boolean = true

        override fun next(): Message? {
            val json = gson.fromJson<JsonElement>(reader)
            val name = json["type"].asString
            val type = MessageCodec[name]?.messageType
            return if (type == null) null
            else gson.fromJson<Message>(json, type.java)
        }
    }
}
