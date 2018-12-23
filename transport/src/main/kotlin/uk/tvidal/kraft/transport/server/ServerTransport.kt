package uk.tvidal.kraft.transport.server

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.codec.SocketCodecFactory
import uk.tvidal.kraft.codec.SocketMessageWriter
import uk.tvidal.kraft.logging.KRaftLogging
import uk.tvidal.kraft.message.Message
import uk.tvidal.kraft.message.transport.ConnectMessage
import uk.tvidal.kraft.message.transport.TransportMessage
import uk.tvidal.kraft.threadFactory
import uk.tvidal.kraft.transport.MessageReceiver
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors.newCachedThreadPool
import java.util.concurrent.Executors.newSingleThreadExecutor
import kotlin.concurrent.thread

class ServerTransport(
    private val node: RaftNode,
    port: Int,
    private val messageReceiver: MessageReceiver,
    private val codecFactory: SocketCodecFactory
) {
    companion object : KRaftLogging()

    private val serverSocket = ServerSocket(port)

    private val executor = newCachedThreadPool(threadFactory("$node-Reader"))

    private val writer = newSingleThreadExecutor { Thread(it, "$node-Writer") }

    private val writers = ConcurrentHashMap<RaftNode, SocketMessageWriter>()

    @Volatile
    private var running: Boolean = true

    private val serverThread = thread(name = "$node-Server") {
        while (running) {
            log.tryCatch {
                val socket = serverSocket.accept()
                log.info { "Client Connected: ${socket.inetAddress}" }
                read(socket)
            }
        }
    }

    private fun read(socket: Socket) {
        executor.submit {
            val messageReader = codecFactory.reader(socket)
            while (running && socket.isConnected) {
                log.tryCatch {
                    messageReader
                        .asSequence()
                        .filterNotNull()
                        .forEach { receiveMessage(socket, it) }
                }
            }
        }
    }

    private fun receiveMessage(socket: Socket, msg: Message) {
        when (msg) {
            is ConnectMessage -> writers[msg.from] = codecFactory.writer(socket)
            !is TransportMessage -> messageReceiver.offer(msg)
        }
    }

    fun write(to: RaftNode, message: Message) {
        writers[to]?.writeAsync(message)
    }

    private fun SocketMessageWriter.writeAsync(message: Message) {
        writer.submit {
            log.tryCatch {
                write(message)
            }
        }
    }
}
