package net.tvidal.kraft.transport

import net.tvidal.kraft.domain.RaftNode
import net.tvidal.kraft.message.Message
import net.tvidal.kraft.message.MessageType
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import kotlin.test.assertTrue

class DisruptorTransportTest {

    companion object {
        val PRODUCER = RaftNode(0)
        val CONSUMER = RaftNode(1)
        val MESSAGE = object : Message {
            override val type = MessageType.NONE
            override val from = PRODUCER
        }
    }

    lateinit var transport: KRaftTransport

    @BeforeMethod
    fun beforeTest() {
        transport = DisruptorTransport()
    }

    @Test(expectedExceptions = arrayOf(IllegalArgumentException::class))
    fun shouldThrowErrorIfSenderNotRegistered() {
        transport.sender(PRODUCER)
    }

    @Test
    fun shouldReturnSameRegisteredConsumerForNode() {
        val placeHolder = CompletableFuture<Message>()
        val consumer = Consumer<Message> { placeHolder.complete(it) }
        transport.register(CONSUMER, consumer)
        transport.sender(CONSUMER).send(MESSAGE)
        assertTrue(MESSAGE === placeHolder.get())
    }
}
