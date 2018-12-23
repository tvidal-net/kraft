package uk.tvidal.kraft.transport

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.codec.SocketMessageWriter
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
    config: NetworkTransportConfig
) : Closeable {
    companion object : KRaftLogging()

    private val serverSocket = ServerSocket(config.host.port)

    private val node = config.node
    private val readerThread = config.readerThread
    private val writerThread = config.writerThread
    private val codec = config.codec
    private val messages = config.messageReceiver

    private val writers = ConcurrentHashMap<RaftNode, SocketMessageWriter>()

    @Volatile
    private var running: Boolean = true

    init {
        config.serverThread.retry(this::running, maxAttempts = 0, name = "Server") {
            val socket = serverSocket.accept()
            log.info { "Client Connected: ${socket.inetAddress}" }
            read(socket)
        }
    }

    private fun read(socket: Socket) {
        val reader = codec.reader(socket)
        readerThread.retry({ running && socket.isConnected }, name = "Server (${socket.inetAddress})") {
            for (msg in reader) {
                if (msg != null) {
                    receiveMessage(socket, msg)
                }
            }
        }
    }

    private fun receiveMessage(socket: Socket, msg: Message) {
        writers.computeIfAbsent(msg.from) { codec.writer(socket) }
        if (msg !is TransportMessage) {
            messages.offer(msg)
        }
    }

    fun write(to: RaftNode, message: Message) {
        writers[to]?.writeAsync(message)
    }

    private fun SocketMessageWriter.writeAsync(message: Message) {
        writerThread.tryCatch {
            write(message)
        }
    }

    override fun close() {
        running = false
        serverSocket.close()
    }
}
