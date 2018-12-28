package uk.tvidal.kraft.transport.socket

import uk.tvidal.kraft.iterable
import uk.tvidal.kraft.message.Message
import java.io.Closeable

interface KRaftConnection : Closeable {

    fun read(): Iterable<Message>
    fun write(message: Message)

    companion object {
        val NOOP = object : KRaftConnection {
            override fun read(): Iterable<Message> {
                TODO("not implemented")
                iterable {  }
            }

            override fun write(message: Message) {
                TODO("not implemented")
            }

            override fun close() {
                TODO("not implemented")
            }
        }
    }
}
