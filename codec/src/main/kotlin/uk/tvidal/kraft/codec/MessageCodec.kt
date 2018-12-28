package uk.tvidal.kraft.codec

import uk.tvidal.kraft.codec.binary.BinaryCodec.MessageProto
import uk.tvidal.kraft.codec.binary.ProtoMessageCodec.decode
import uk.tvidal.kraft.codec.binary.ProtoMessageCodec.encode
import uk.tvidal.kraft.codec.json.JsonMessageReader
import uk.tvidal.kraft.codec.json.JsonMessageWriter
import uk.tvidal.kraft.iterable
import uk.tvidal.kraft.message.Message
import uk.tvidal.kraft.message.MessageType
import uk.tvidal.kraft.message.client.ClientMessageType
import uk.tvidal.kraft.message.raft.RaftMessageType
import uk.tvidal.kraft.message.transport.TransportMessageType
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

    fun jsonReader(stream: InputStream) = JsonMessageReader(
        stream.reader()
    )

    fun jsonWriter(stream: OutputStream) = JsonMessageWriter(
        stream.writer()
    )

    fun binaryReader(stream: InputStream) = iterable {
        decode(
            MessageProto.parseDelimitedFrom(stream)
        )
    }

    fun binaryWriter(stream: OutputStream): (Message) -> Unit = {
        encode(it).writeDelimitedTo(stream)
        stream.flush()
    }
}
