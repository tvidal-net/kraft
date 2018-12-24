package uk.tvidal.kraft

import uk.tvidal.kraft.client.producer.KRaftProducer
import uk.tvidal.kraft.client.producer.KRaftProducerImpl
import uk.tvidal.kraft.client.producer.ProducerMode
import uk.tvidal.kraft.client.producer.ProducerMode.FIRE_AND_FORGET
import uk.tvidal.kraft.transport.MessageSender

fun producer(
    sender: MessageSender,
    mode: ProducerMode = FIRE_AND_FORGET
): KRaftProducer = KRaftProducerImpl(
    server = sender,
    mode = mode,
    node = sender.self
)
