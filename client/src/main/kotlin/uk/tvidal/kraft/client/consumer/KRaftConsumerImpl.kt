package uk.tvidal.kraft.client.consumer

import uk.tvidal.kraft.client.AbstractKRaftClient
import uk.tvidal.kraft.logging.KRaftLogging
import uk.tvidal.kraft.message.client.ConsumerAckMessage
import uk.tvidal.kraft.message.client.ConsumerDataMessage
import uk.tvidal.kraft.message.client.ConsumerRegisterMessage
import uk.tvidal.kraft.retry
import uk.tvidal.kraft.transport.MessageReceiver
import uk.tvidal.kraft.transport.MessageSender
import java.util.concurrent.ExecutorService

class KRaftConsumerImpl internal constructor(
    server: MessageSender,
    index: Long,
    private val messages: MessageReceiver,
    private val consumerThread: ExecutorService,
    private val receiver: ConsumerReceiver
) : AbstractKRaftClient(server), KRaftConsumer {

    private companion object : KRaftLogging()

    @Volatile
    var active: Boolean = true
        private set

    @Volatile
    var index: Long = index
        private set

    init {
        register()
    }

    private fun register(fromIndex: Long = index) {
        server.send(
            ConsumerRegisterMessage(
                from = self,
                fromIndex = fromIndex
            )
        )
    }

    private fun read() {
        consumerThread.retry(this::active, name = "Consumer ($self)") {
            val message = messages.poll()
            when (message) {
                is ConsumerDataMessage -> consumeData(message)
                else -> log.warn { "$self unexpected message=$message" }
            }
        }
    }

    private fun consumeData(message: ConsumerDataMessage) {
        if (receiver(message)) {
            server.send(
                ConsumerAckMessage(
                    from = self,
                    index = message.lastIndex
                )
            )
        }
    }
}
