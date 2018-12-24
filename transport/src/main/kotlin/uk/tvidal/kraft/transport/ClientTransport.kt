package uk.tvidal.kraft.transport

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.logging.KRaftLogging
import uk.tvidal.kraft.message.Message
import uk.tvidal.kraft.message.transport.ConnectMessage
import uk.tvidal.kraft.retry
import uk.tvidal.kraft.tryCatch
import java.io.Closeable
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.CompletableFuture

class ClientTransport(
    val node: RaftNode,
    val config: NetworkTransportConfig
) : Closeable {
    companion object : KRaftLogging()

    @Volatile
    private var running: Boolean = true

    @Volatile
    private var connection: SocketConnection? = null

    private val writer: SocketMessageWriter
        get() = (connection ?: connect()).writer

    init {
        write(ConnectMessage(config.node))
    }

    fun write(message: Message) {
        config.writerThread.tryCatch {
            writer(message)
        }
    }

    private fun connect(): SocketConnection {
        val name = "Client [${config.node} -> $node]"
        val future = CompletableFuture<SocketConnection>()
        config.readerThread.retry(this::running, maxAttempts = 0, name = name) {
            val host = config[node]
            connection = SocketConnection(host)
            log.info { "$name connected to $host" }
            if (!future.isDone) future.complete(connection)
            with(connection!!) {
                reader.map(config.messageReceiver::offer)
            }
        }
        return future.get()
    }

    override fun close() {
        running = false
        val cnn = connection
        connection = null
        cnn?.close()
    }

    private inner class SocketConnection(host: InetSocketAddress) : Closeable {
        val socket = Socket(host.address, host.port)
        val reader = config.codec.reader(socket)
        val writer = config.codec.writer(socket)

        override fun close() {
            socket.close()
        }
    }
}
