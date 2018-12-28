package uk.tvidal.kraft.codec.binary

import com.github.salomonbrys.kotson.contains
import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.registerTypeAdapter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.codec.MessageCodec
import uk.tvidal.kraft.codec.binary.BinaryCodec.MessageProto
import uk.tvidal.kraft.codec.json.adapter.RaftNodeAdapter
import uk.tvidal.kraft.message.DataMessage
import uk.tvidal.kraft.message.Message
import uk.tvidal.kraft.message.MessageType
import uk.tvidal.kraft.message.Payload
import uk.tvidal.kraft.storage.KRaftEntries
import uk.tvidal.kraft.storage.entries
import kotlin.reflect.KParameter
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor

object ProtoMessageCodec {

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter<RaftNode>(RaftNodeAdapter())
        .registerTypeAdapterFactory(ProtoTypeAdapterFactory)
        .create()

    private val decoders = MessageCodec
        .messageTypes
        .values
        .associateWith(::MessageDecoder)

    fun encode(message: Message): MessageProto = MessageProto.newBuilder()
        .setMessageType(message.type.name)
        .setMessage(gson.toJson(message))
        .setFrom(message.from.toProto())
        .setPayload(message)
        .build()

    private fun MessageProto.Builder.setPayload(message: Message): MessageProto.Builder {
        if (message is DataMessage) {
            val payload = DataMessage::data.call(message)
            payload.forEach { addEntries(it.toProto()) }
        }
        return this
    }

    fun decode(proto: MessageProto): Message? = MessageCodec[proto.messageType]?.let {
        decoders[it]?.decode(proto)
    }

    @Suppress("UNCHECKED_CAST")
    private class MessageDecoder(val type: MessageType) {

        val messageType = type.messageType!!

        val constructor = messageType.primaryConstructor!!

        val params = constructor
            .parameters
            .associateWith { it.type.rawType }

        val payloadProperty = messageType
            .declaredMemberProperties
            .firstOrNull { it.findAnnotation<Payload>() != null }

        fun decode(proto: MessageProto): Message? {
            val args = proto.constructorArgs(
                payloadProperty?.name to proto.entries(),
                Message::from.name to proto.from.toNode(),
                Message::type.name to type
            )
            return constructor.callBy(args)
        }

        private fun MessageProto.constructorArgs(vararg properties: Pair<String?, Any?>): Map<KParameter, Any?> {
            val baseProperties = properties.toMap()
            val extraProperties = gson.fromJson<JsonObject>(message)
            return params.entries.associate {
                val name = it.key.name!!
                it.key to when (name) {
                    in baseProperties -> baseProperties[name]
                    in extraProperties -> gson.fromJson(extraProperties[name], it.value.java)
                    else -> null
                }
            }
        }

        private fun MessageProto.entries(): KRaftEntries = entriesList
            .map(::entryOf)
            .let { entries(it) }
    }
}
