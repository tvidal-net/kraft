package uk.tvidal.kraft.codec.binary

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import uk.tvidal.kraft.message.Message
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSuperclassOf

object ProtoTypeAdapterFactory : TypeAdapterFactory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        val kClass = type.kotlin
        return if (Message::class.isSuperclassOf(kClass)) {
            val messageClass = kClass as KClass<out Message>
            val properties = extraAttributes(messageClass)
                .associateWith { gson.getAdapter(it) }

            return ProtoMessageAdapter(properties) as TypeAdapter<T>
        } else null
    }

    private fun extraAttributes(messageClass: KClass<out Message>) = messageClass
        .declaredMemberProperties
        .asSequence()
        .filterNot { it.isOpen && it.findAnnotation<Transient>() == null }

    private class ProtoMessageAdapter(
        val properties: Map<MessageProperty<*>, TypeAdapter<Any?>>
    ) : TypeAdapter<Message>() {

        override fun write(writer: JsonWriter, message: Message?) {
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

        override fun read(reader: JsonReader?): Message {
            throw UnsupportedOperationException("This adapter is only used to write json")
        }
    }
}
