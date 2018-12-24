package uk.tvidal.kraft

import uk.tvidal.kraft.client.localClientNode
import uk.tvidal.kraft.client.localNetworkSiteAddress
import uk.tvidal.kraft.codec.json.JsonMessageReader
import uk.tvidal.kraft.codec.json.MessageCodec
import uk.tvidal.kraft.logging.KRaftLogger
import uk.tvidal.kraft.message.raft.VoteMessage
import uk.tvidal.kraft.transport.networkTransport
import java.lang.Thread.sleep
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors.newCachedThreadPool
import java.util.concurrent.atomic.AtomicInteger

private val threadCount = AtomicInteger()

private val executor = newCachedThreadPool { Thread(it, "SocketReader-${threadCount.getAndIncrement()}") }

private val log by lazy { KRaftLogger {} }

private fun read(socket: Socket) {
    val inputStream = socket.getInputStream()
    val reader = inputStream.reader()
    val jsonReader = JsonMessageReader(reader)
    executor.submit {
        try {
            while (socket.isConnected) {
                for (msg in jsonReader) {
                    log.info { "Message Received: $msg" }
                }
                log.info { "No more messages" }
            }
            log.info { "Client closed: $socket" }
        } catch (e: Throwable) {
            log.error(e)
        }
    }
}

private fun server(port: Int) {
    val serverSocket = ServerSocket(port)
    log.info { "Listening to connections on port $port" }
    executor.submit {
        try {
            while (!serverSocket.isClosed) {
                val socket = serverSocket.accept()
                log.info { "Client Connected: ${socket.inetAddress}" }
                read(socket)
            }
            log.info { "Server closed" }
        } catch (e: Throwable) {
            log.error(e)
        }
    }
}

object NewTest {
    @JvmStatic
    fun main(args: Array<String>) {
        logbackConsoleConfiguration()
        val nodes = raftNodes(2)
        val (n0, n1) = nodes
        val transport = networkTransport(nodes)
        val message = VoteMessage(localClientNode(), 1L, false)

        val s0 = transport[n0]!!.sender(n1)
        val s1 = transport[n1]!!.sender(n0)

        s0.let { }
        s1.let { message }

        /*
        s0.send(message)
        s1.send(message)

        sleep(500)
        s0.respond(message)
        s1.respond(message)
        // */
    }
}

fun main(args: Array<String>) {
    logbackConsoleConfiguration()
    //    server(1801)

    val node = localClientNode()
    val host = localNetworkSiteAddress

    var count = 0L

    singleThreadPool("TransportTest").retry(maxAttempts = 0) {
        val socket = Socket(host, 1801)
        val writer = MessageCodec.jsonWriter(socket.getOutputStream())
        while (true) {
            val msg = VoteMessage(node, count, false)
            log.info { "Written message ${count++}" }
            writer.write(msg)
            sleep(300)
        }
    }
}
