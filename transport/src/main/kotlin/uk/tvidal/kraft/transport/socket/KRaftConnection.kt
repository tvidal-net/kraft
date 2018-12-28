package uk.tvidal.kraft.transport.socket

import uk.tvidal.kraft.logging.KRaftLogging
import uk.tvidal.kraft.message.Message
import uk.tvidal.kraft.javaClassName
import java.io.Closeable

interface KRaftConnection : Closeable {

    val read: Iterable<Message>
    fun write(message: Message)

    companion object {
        val NOOP: KRaftConnection = object : KRaftConnection, KRaftLogging() {

            override val read: Iterable<Message> = emptyList()

            override fun write(message: Message) {
                log.warn { "lost message $message" }
            }

            override fun close() {}

            override fun toString() = "$javaClassName.NOOP"
        }
    }
}
