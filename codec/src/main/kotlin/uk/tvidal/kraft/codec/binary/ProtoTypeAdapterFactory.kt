package uk.tvidal.kraft.codec.binary

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import uk.tvidal.kraft.message.Message
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSuperclassOf

object ProtoTypeAdapterFactory : TypeAdapterFactory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        val kClass = type.kotlin
        return if (Message::class.isSuperclassOf(kClass)) {
            val messageClass = kClass as KClass<out Message>
            val properties: Map<MessageProperty<*>, TypeAdapter<Any?>> = messageClass
                .declaredMemberProperties
                .filterNot(::isPayloadProperty)
                .associateWith { gson.getAdapter(it) }

            val adapter = ProtoMessageAdapter(messageClass, properties)
            return adapter as TypeAdapter<T>
        } else null
    }
}
