package uk.tvidal.kraft.transport

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.logging.KRaftLogging
import uk.tvidal.kraft.message.Message
import uk.tvidal.kraft.message.transport.TransportMessage
import uk.tvidal.kraft.retry
import uk.tvidal.kraft.tryCatch
import java.io.Closeable
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap

class ServerTransport(
    val config: NetworkTransportConfig
) : Closeable {
    companion object : KRaftLogging()

    private val serverSocket = ServerSocket(config.host.port)

    private val writers = ConcurrentHashMap<RaftNode, SocketMessageWriter>()

    @Volatile
    private var running: Boolean = true

    private val node get() = config.self

    init {
        log.info { "Server [$node] waiting for connections on port ${config.host.port}" }
        config.serverThread.retry(this::running, maxAttempts = 0, name = "Server") {
            val socket = serverSocket.accept()
            log.debug { "incoming connection ${socket.inetAddress}:${socket.port}" }
            read(socket)
        }
    }

    private fun read(socket: Socket) {
        config.readerThread.retry(this::running, maxAttempts = 0, name = "Server ($socket)") {
            config
                .codec
                .reader(socket)
                .forEach { receiveMessage(socket, it) }
        }
    }

    private fun receiveMessage(socket: Socket, message: Message?) {
        if (message != null && checkValidClient(message.from, socket)) {
            val from = message.from
            writers.computeIfAbsent(from) {
                clientConnected(from, socket)
            }
            if (message !is TransportMessage) {
                config.messageReceiver.offer(message)
            }
        }
    }

    private fun checkValidClient(from: RaftNode, socket: Socket): Boolean {
        val configAddress = config.cluster[from]?.address
        val check = configAddress == null || configAddress == socket.inetAddress
        if (!check) {
            log.warn { "message in $socket pretending to be from $from (expected=$configAddress actual=${socket.inetAddress})" }
        }
        return check
    }

    private fun clientConnected(from: RaftNode, socket: Socket): SocketMessageWriter {
        log.info { "Server [$node <- $from] client connected ($socket)" }
        return config
            .codec
            .writer(socket)
    }

    fun write(to: RaftNode, message: Message) {
        config.writerThread.tryCatch {
            val writer = writers[to]
            if (writer != null) writer(message)
            else log.warn { "$node attempted to send message to unknown node $to" }
        }
    }

    override fun close() {
        running = false
        serverSocket.close()
    }
}
