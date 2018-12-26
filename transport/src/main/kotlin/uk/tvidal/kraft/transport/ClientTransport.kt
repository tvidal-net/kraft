package uk.tvidal.kraft.transport

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.RetryDelay.Companion.FOREVER
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

    private val host get() = config[node]

    @Volatile
    private var running: Boolean = true

    @Volatile
    private var connection: SocketConnection? = null

    private val reader get() = connection!!.reader
    private val messages get() = config.messageReceiver

    private val writer: SocketMessageWriter
        get() = (connection ?: connect()).writer

    init {
        write(ConnectMessage(config.self))
    }

    fun write(message: Message) {
        config.writerThread.tryCatch {
            writer(message)
        }
    }

    private fun connect(): SocketConnection {
        val name = "Client [${config.self} -> $node]"
        val future = CompletableFuture<SocketConnection>()
        config.readerThread.retry(this::running, FOREVER, name = name) {
            connection = SocketConnection(host)
            log.info { "$name connected to ${connection!!.socket}" }
            if (!future.isDone) future.complete(connection)
            reader.forEach(this::receiveMessage)
        }
        return future.get()
    }

    private fun receiveMessage(message: Message?) {
        val from = message?.from
        if (message?.from != node) {
            log.warn { "message in ${connection?.socket} pretending to be from $from" }
            return
        }
        messages.offer(message)
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
