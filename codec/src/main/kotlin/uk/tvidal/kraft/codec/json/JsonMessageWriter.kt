package uk.tvidal.kraft.codec.json

import com.google.gson.stream.JsonWriter
import uk.tvidal.kraft.message.Message
import java.io.Writer

class JsonMessageWriter(writer: Writer) {

    private val writer: JsonWriter = gson.newJsonWriter(writer)

    fun write(msg: Message) {
        val json = gson.toJsonTree(msg)
        gson.toJson(json, writer)
        writer.flush()
    }
}
