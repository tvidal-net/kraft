package uk.tvidal.kraft.transport

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.RetryDelay.Companion.FOREVER
import uk.tvidal.kraft.codec.SocketCodecFactory
import uk.tvidal.kraft.javaClassName
import uk.tvidal.kraft.logging.KRaftLogging
import uk.tvidal.kraft.message.Message
import uk.tvidal.kraft.message.transport.ConnectMessage
import uk.tvidal.kraft.retry
import uk.tvidal.kraft.transport.socket.KRaftConnection
import uk.tvidal.kraft.transport.socket.KRaftConnection.Companion.NOOP
import uk.tvidal.kraft.transport.socket.SocketConnection
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
    private val self = config.self

    @Volatile
    var isActive: Boolean = true
        private set

    @Volatile
    private var connection: KRaftConnection = NOOP

    init {
        with(config) {
            connect(codec, host)
        }
    }

    fun write(message: Message) {
        writerThread.tryCatch {
            connection.write(message)
        }
    }

    private fun connect(codec: SocketCodecFactory, host: InetSocketAddress) {
        val name = toString()
        readerThread.retry(::isActive, FOREVER, name = name) {
            log.debug { "$name connecting $host" }
            connection = SocketConnection(codec, Socket(host.address, host.port))

            log.info { "$name connected $connection" }
            with(connection) {
                write(ConnectMessage(self))
                read.forEach(::receiveMessage)
            }
        }
    }

    private fun receiveMessage(message: Message) {
        if (message.from != node) {
            log.warn { "[$self] => $node dropping unknown message $message" }
            return
        }
        messages.offer(message)
    }

    override fun close() {
        isActive = false
        connection.close()
    }

    override fun toString() = "$javaClassName[$self] => $node"
}
