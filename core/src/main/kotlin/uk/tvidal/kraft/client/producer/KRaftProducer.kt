package uk.tvidal.kraft.client.producer

import uk.tvidal.kraft.client.KRaftClient
import uk.tvidal.kraft.storage.KRaftEntries
import uk.tvidal.kraft.storage.KRaftEntry
import uk.tvidal.kraft.storage.entryOf
import java.nio.charset.Charset
import java.nio.charset.Charset.defaultCharset
import java.util.concurrent.Future

interface KRaftProducer : KRaftClient {

    val mode: ProducerMode

    fun publish(payload: String, term: Long = 0L, charset: Charset = defaultCharset()) = publish(entryOf(payload, term, charset))

    fun publish(payload: ByteArray, term: Long = 0L) = publish(entryOf(payload, term))

    fun publish(entry: KRaftEntry) = publish(entry.toEntries())

    fun publish(entries: KRaftEntries): Future<ProducerResponse>
}
