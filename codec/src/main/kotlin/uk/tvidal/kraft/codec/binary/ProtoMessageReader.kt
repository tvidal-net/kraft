package uk.tvidal.kraft.codec.binary

import uk.tvidal.kraft.codec.binary.BinaryCodec.MessageProto
import uk.tvidal.kraft.message.Message
import java.io.InputStream

class ProtoMessageReader(val inputStream: InputStream) : Iterable<Message?>, Iterator<Message?> {

    override fun iterator(): Iterator<Message?> = this

    override fun hasNext(): Boolean = true

    override fun next(): Message? = MessageProto
        .parseDelimitedFrom(inputStream)
        ?.toMessage()
}
