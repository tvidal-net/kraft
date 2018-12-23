package uk.tvidal.kraft

import uk.tvidal.kraft.client.localClientNode
import uk.tvidal.kraft.client.localNetworkSiteAddress
import uk.tvidal.kraft.codec.json.JsonMessageReader
import uk.tvidal.kraft.codec.json.MessageCodec
import uk.tvidal.kraft.logging.KRaftLogger
import uk.tvidal.kraft.message.raft.VoteMessage
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

fun main(args: Array<String>) {
    logbackConfigurationFile = LOGBACK_CONSOLE
    //    server(1801)

    val node = localClientNode()
    val host = localNetworkSiteAddress()

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
