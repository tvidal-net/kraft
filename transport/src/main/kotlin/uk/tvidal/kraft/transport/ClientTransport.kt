package uk.tvidal.kraft.transport

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.logging.KRaftLogging
import uk.tvidal.kraft.message.Message
import uk.tvidal.kraft.retry
import uk.tvidal.kraft.tryCatch
import java.io.Closeable
import java.net.Socket

class ClientTransport(
    node: RaftNode,
    config: NetworkTransportConfig
) : Closeable {
    companion object : KRaftLogging()

    private val socket = with(config[node]) {
        Socket(address, port)
    }

    private val writerThread = config.writerThread

    @Volatile
    private var running: Boolean = true

    private val reader = config.codec.reader(socket)
    private val writer = config.codec.writer(socket)

    init {
        val messages = config.messageReceiver
        config.readerThread.retry({ running && socket.isConnected }, maxAttempts = 0, name = "Client (${socket.inetAddress})") {
            for (msg in reader) {
                if (msg != null) {
                    messages.offer(msg)
                }
            }
        }
    }

    fun write(message: Message) {
        writerThread.tryCatch {
            writer(message)
        }
    }

    override fun close() {
        running = false
        socket.close()
    }
}
