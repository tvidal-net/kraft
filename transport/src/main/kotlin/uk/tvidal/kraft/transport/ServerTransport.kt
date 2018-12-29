package uk.tvidal.kraft.transport

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.RetryDelay.Companion.FOREVER
import uk.tvidal.kraft.javaClassName
import uk.tvidal.kraft.logging.KRaftLogging
import uk.tvidal.kraft.message.Message
import uk.tvidal.kraft.message.transport.HeartBeatMessage
import uk.tvidal.kraft.message.transport.TransportMessage
import uk.tvidal.kraft.retry
import uk.tvidal.kraft.transport.socket.SocketConnection
import uk.tvidal.kraft.tryCatch
import java.io.Closeable
import java.lang.System.currentTimeMillis
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap

class ServerTransport(
    config: NetworkTransportConfig
) : Closeable {

    internal companion object : KRaftLogging()

    private val heartbeatTimeout = config.heartBeatTimeout
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
        readerThread.retry(::isActive, FOREVER, name = socket.toString()) {
            SocketConnection(codec, socket).use { connection ->
                for (message in connection.read) {
                    if (validateClient(socket, message.from)) {
                        receiveMessage(message)
                    }
                }
            }
        }
    }

    private fun validateClient(socket: Socket, from: RaftNode): Boolean {
        val address = socket.inetAddress
        val expected = cluster[from]?.address
        return if (expected != null && expected != address) {
            log.warn { "[$self] <- $from invalid address=$address expected=$expected" }
            false
        } else {
            writers.computeIfAbsent(from) {
                log.info { "[$self] <- $from client connected $socket" }
                codec.writer(socket)
            }
            true
        }
    }

    private fun receiveMessage(message: Message) {
        when (message) {
            is HeartBeatMessage -> heartbeat(message)
            !is TransportMessage -> messages.offer(message)
        }
    }

    private fun heartbeat(message: HeartBeatMessage) {
        if (message.ping) {
            val response = message.copy(self, ping = false)
            writers[message.from]?.invoke(response)
        }
        val lag = currentTimeMillis() - message.time
        if (lag > heartbeatTimeout) {
            log.warn { "[$self] <- ${message.from} time skew is ${lag}ms" }
        }
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
        log.info { "closed $this" }
    }

    override fun toString() = "$javaClassName[$self]"
}
