package uk.tvidal.kraft

import uk.tvidal.kraft.client.clientNode
import uk.tvidal.kraft.client.consumer.ConsumerReceiver
import uk.tvidal.kraft.client.consumer.KRaftConsumer
import uk.tvidal.kraft.client.consumer.KRaftConsumerImpl
import uk.tvidal.kraft.client.consumer.LAST_INDEX
import uk.tvidal.kraft.client.producer.KRaftProducer
import uk.tvidal.kraft.client.producer.KRaftProducerImpl
import uk.tvidal.kraft.client.producer.ProducerMode
import uk.tvidal.kraft.client.producer.ProducerMode.FIRE_AND_FORGET
import uk.tvidal.kraft.transport.BlockingMessageReceiver
import uk.tvidal.kraft.transport.messageSender
import java.net.InetSocketAddress
import java.util.concurrent.ExecutorService

val consumerThreadPool = cachedThreadPool("KRaftConsumer")

fun producer(
    server: Pair<RaftNode, InetSocketAddress>,
    mode: ProducerMode = FIRE_AND_FORGET,
    self: RaftNode = clientNode("Producer")
): KRaftProducer = KRaftProducerImpl(
    server = messageSender(server, self),
    mode = mode
)

fun consumer(
    server: Pair<RaftNode, InetSocketAddress>,
    index: Long = LAST_INDEX,
    self: RaftNode = clientNode("Consumer"),
    threadPool: ExecutorService = consumerThreadPool,
    receiver: ConsumerReceiver
): KRaftConsumer = BlockingMessageReceiver().let { messageReceiver ->
    KRaftConsumerImpl(
        server = messageSender(server, self, messageReceiver),
        index = index,
        messages = messageReceiver,
        consumerThread = threadPool,
        receiver = receiver
    )
}
