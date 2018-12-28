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

class ClientTransport(
    val node: RaftNode,
    config: NetworkTransportConfig
) : Closeable {

    internal companion object : KRaftLogging()

    private val writerThread = config.writerThread
    private val readerThread = config.readerThread
    private val messages = config.messageReceiver
    private val codec = config.codec
    private val self = config.self
    private val host = config[node]

    @Volatile
    var isOpen: Boolean = true
        private set

    @Volatile
    private lateinit var connection: SocketConnection

    init {
        connect()
    }

    fun write(message: Message) {
        writerThread.tryCatch {
            connection.write(message)
        }
    }

    private fun connect() {
        val name = "Client[$self] => $node"
        readerThread.retry(this::isOpen, FOREVER, name = name) {
            log.debug { "$name connecting $host" }
            connection = SocketConnection(host)

            log.info { "$name connected $connection" }
            with(connection) {
                write(ConnectMessage(self))
                reader.forEach(::receiveMessage)
            }
        }
    }

    private fun receiveMessage(message: Message?) {
        val from = message?.from
        if (message?.from != node) {
            log.warn { "message in $connection pretending to be from $from" }
            return
        }
        messages.offer(message)
    }

    override fun close() {
        isOpen = false
        if (this::connection.isInitialized) {
            connection.close()
        }
    }

    private inner class SocketConnection(host: InetSocketAddress) : Closeable {
        private val socket = Socket(host.address, host.port)
        val reader = codec.reader(socket)
        val write = codec.writer(socket)

        override fun close() {
            socket.close()
        }

        override fun toString() = socket.toString()
    }
}
