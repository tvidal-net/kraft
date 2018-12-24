package uk.tvidal.kraft.client.consumer

import uk.tvidal.kraft.client.KRaftClient

interface KRaftConsumer : KRaftClient {

    fun subscribe(index: Long)
}
