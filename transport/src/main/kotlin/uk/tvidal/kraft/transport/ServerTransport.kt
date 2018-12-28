package uk.tvidal.kraft.transport

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.RetryDelay.Companion.FOREVER
import uk.tvidal.kraft.javaClassName
import uk.tvidal.kraft.logging.KRaftLogging
import uk.tvidal.kraft.message.Message
import uk.tvidal.kraft.message.transport.TransportMessage
import uk.tvidal.kraft.retry
import uk.tvidal.kraft.transport.socket.SocketConnection
import uk.tvidal.kraft.tryCatch
import java.io.Closeable
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap

class ServerTransport(
    config: NetworkTransportConfig
) : Closeable {

    internal companion object : KRaftLogging()

    private val writerThread = config.writerThread
    private val readerThread = config.readerThread
    private val messages = config.messageReceiver
    private val cluster = config.cluster
    private val codec = config.codec

    val self = config.self

    private val serverSocket = ServerSocket(config.host.port)

    private val writers = ConcurrentHashMap<RaftNode, MessageWriter>()

    @Volatile
    var isActive: Boolean = true
        private set

    init {
        log.info { "[$self] waiting for connections on port ${config.host.port}" }
        config.serverThread.retry(::isActive, FOREVER, name = toString()) {
            val socket = serverSocket.accept()
            log.debug { "[$self] incoming connection ${socket.inetAddress}:${socket.port}" }
            read(socket)
        }
    }

    private fun read(socket: Socket) {
        val connection = SocketConnection(codec, socket)
        readerThread.retry(::isActive, FOREVER, name = connection.toString()) {
            for (message in connection.read) {
                if (validateClient(socket.inetAddress, message.from)) {
                    receiveMessage(socket, message)
                }
            }
        }
    }

    private fun receiveMessage(socket: Socket, message: Message) {
        val from = message.from
        writers.computeIfAbsent(from) {
            log.info { "[$self] <- $from client connected $socket" }
            codec.writer(socket)
        }
        if (message !is TransportMessage) {
            messages.offer(message)
        }
    }

    private fun validateClient(address: InetAddress, from: RaftNode): Boolean {
        val expected = cluster[from]?.address
        return if (expected != null && expected != address) {
            log.warn { "[$self] <- $from invalid address=$address expected=$expected" }
            false
        } else true
    }

    fun write(to: RaftNode, message: Message) {
        writerThread.tryCatch {
            val writer = writers[to]
            if (writer != null) writer.invoke(message)
            else log.warn { "[$self] -> $to attempted to send message to unknown node" }
        }
    }

    override fun close() {
        isActive = false
        serverSocket.close()
    }

    override fun toString() = "$javaClassName[$self]"
}
