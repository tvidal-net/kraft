package uk.tvidal.kraft.codec

import uk.tvidal.kraft.codec.json.JsonMessageReader
import uk.tvidal.kraft.codec.json.JsonMessageWriter
import uk.tvidal.kraft.message.Message
import uk.tvidal.kraft.message.MessageType
import uk.tvidal.kraft.message.client.ClientMessageType
import uk.tvidal.kraft.message.raft.RaftMessageType
import uk.tvidal.kraft.message.transport.TransportMessageType
import java.io.InputStream
import java.io.OutputStream
import kotlin.reflect.KClass

object MessageCodec {

    val messageTypes = register(RaftMessageType.values()) +
        register(ClientMessageType.values()) +
        register(TransportMessageType.values())

    private fun register(types: Array<out MessageType>): Map<String, MessageType> = types
        .asSequence()
        .filter { it.messageType != null }
        .associate { it.name to it }

    operator fun get(name: String): KClass<out Message>? = messageTypes[name]?.messageType

    fun jsonReader(stream: InputStream) = JsonMessageReader(
        stream.reader()
    )

    fun jsonWriter(stream: OutputStream) = JsonMessageWriter(
        stream.writer()
    )
}
