package uk.tvidal.kraft

import uk.tvidal.kraft.client.clientNode
import uk.tvidal.kraft.client.producer.KRaftProducer
import uk.tvidal.kraft.client.producer.KRaftProducerImpl
import uk.tvidal.kraft.client.producer.ProducerMode
import uk.tvidal.kraft.client.producer.ProducerMode.FIRE_AND_FORGET
import uk.tvidal.kraft.transport.MessageSender

fun producer(
    server: MessageSender,
    mode: ProducerMode = FIRE_AND_FORGET,
    self: RaftNode = clientNode("Producer")
): KRaftProducer = KRaftProducerImpl(
    server = server,
    mode = mode,
    node = self
)
