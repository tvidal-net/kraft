package uk.tvidal.kraft.client.consumer

import uk.tvidal.kraft.message.client.ConsumerDataMessage

const val LAST_INDEX = -1L
const val FIRST_INDEX = 0L

typealias ConsumerReceiver = KRaftConsumer.(ConsumerDataMessage) -> Boolean
