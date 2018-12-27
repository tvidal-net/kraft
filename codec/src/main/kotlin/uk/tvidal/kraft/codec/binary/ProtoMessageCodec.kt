package uk.tvidal.kraft.codec.binary

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
import uk.tvidal.kraft.message.raft.AppendMessage
import uk.tvidal.kraft.message.raft.RaftMessage
import uk.tvidal.kraft.storage.KRaftEntries
import uk.tvidal.kraft.storage.entries
import uk.tvidal.kraft.storage.entryOf
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

object ProtoMessageCodec {

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter<RaftNode>(RaftNodeAdapter())
        .registerTypeAdapterFactory(ProtoTypeAdapterFactory)
        .create()

    fun encode(message: Message): MessageProto = MessageProto.newBuilder()
        .setMessageType(message.type.name)
        .setMessage(gson.toJson(message))
        .setFrom(message.from.toProto())
        .setTerm(message)
        .setPayload(message)
        .build()

    private fun MessageProto.Builder.setTerm(message: Message): MessageProto.Builder {
        if (message is RaftMessage) term = message.term
        return this
    }

    private fun MessageProto.Builder.setPayload(message: Message): MessageProto.Builder {
        if (message is DataMessage<*>) {
            val payload = DataMessage<KRaftEntries>::data.call(message)
            payload.forEach { addEntries(it.toProto()) }
        }
        return this
    }

    fun decode(proto: MessageProto): Message? {
        val messageType = MessageCodec[proto.messageType]
        if (messageType != null) {
            val json = gson.fromJson(proto.message, JsonObject::class.java)
            val from = proto.from.toNode()
            val term = proto.term

            val constructor = messageType.primaryConstructor!!
            val params = constructor.parameters.associate { it to it.type.rawType }
            val args = params.entries.associate { (param, type) ->
                val element = json[param.name!!]
                val value = gson.fromJson(element, type.java)
                param to value
            }
            return constructor.callBy(args)
        }
        return null
    }

    class MessageDecoder(val type: KClass<out Message>) {

        val constructor = type.primaryConstructor!!

        val params = constructor
            .parameters
            .associateWith { it.type.rawType }

    }

    @JvmStatic
    fun main(args: Array<String>) {
        val message = AppendMessage(
            from = RaftNode(1),
            term = 1801L,
            prevTerm = 1801L,
            prevIndex = 1801L,
            leaderCommitIndex = 200L,
            data = entries(entryOf("Payload", 1L), entryOf(1801L, 2L))
        )

        val encoded = encode(message)
        println(encoded)

        val decoded = decode(encoded)
        assert(decoded == message)
    }
}
