package uk.tvidal.kraft.codec

import uk.tvidal.kraft.codec.binary.BinaryCodec.MessageProto
import uk.tvidal.kraft.codec.binary.toMessage
import uk.tvidal.kraft.codec.binary.toProto
import uk.tvidal.kraft.codec.json.JsonMessageReader
import uk.tvidal.kraft.codec.json.JsonMessageWriter
import uk.tvidal.kraft.iterable
import uk.tvidal.kraft.message.MessageType
import uk.tvidal.kraft.message.client.ClientMessageType
import uk.tvidal.kraft.message.raft.RaftMessageType
import uk.tvidal.kraft.message.transport.TransportMessageType
import uk.tvidal.kraft.transport.MessageReader
import uk.tvidal.kraft.transport.MessageWriter
import java.io.InputStream
import java.io.OutputStream

object MessageCodec {

    val messageTypes = register(RaftMessageType.values()) +
        register(ClientMessageType.values()) +
        register(TransportMessageType.values())

    private fun register(types: Array<out MessageType>): Map<String, MessageType> = types
        .filter { it.messageType != null }
        .associateBy(MessageType::name)

    operator fun get(name: String): MessageType? = messageTypes[name]

    fun jsonReader(stream: InputStream): MessageReader = JsonMessageReader(
        stream.reader()
    )

    fun jsonWriter(stream: OutputStream): MessageWriter = JsonMessageWriter(
        stream.writer()
    )::write

    fun binaryReader(stream: InputStream): MessageReader = iterable {
        MessageProto
            .parseDelimitedFrom(stream)
            .toMessage()
    }

    fun binaryWriter(stream: OutputStream): MessageWriter = {
        it.toProto()
            .writeDelimitedTo(stream)

        stream.flush()
    }
}
