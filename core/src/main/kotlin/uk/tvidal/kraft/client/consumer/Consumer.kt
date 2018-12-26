package uk.tvidal.kraft.client.consumer

import uk.tvidal.kraft.message.client.ConsumerDataMessage

const val LOG_TAIL = -1L
const val LOG_HEAD = 0L

typealias ConsumerReceiver = KRaftConsumer.(ConsumerDataMessage) -> Boolean
