package uk.tvidal.kraft.codec.binary

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import uk.tvidal.kraft.message.Message
import kotlin.reflect.KClass

class ProtoMessageAdapter<T : Message>(
    val type: KClass<out T>,
    val properties: Map<MessageProperty<*>, TypeAdapter<Any?>>
) : TypeAdapter<T>() {

    override fun write(writer: JsonWriter, message: T?) {
        writer.beginObject()
        properties.forEach { property, adapter ->
            val value = property.call(message)
            if (value != null) {
                writer.name(property.name)
                adapter.write(writer, value)
            }
        }
        writer.endObject()
    }

    override fun read(reader: JsonReader?): T {
        TODO("not implemented")
    }
}
