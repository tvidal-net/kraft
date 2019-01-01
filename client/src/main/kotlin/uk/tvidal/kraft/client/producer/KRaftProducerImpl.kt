package uk.tvidal.kraft.client.producer

import uk.tvidal.kraft.client.AbstractKRaftClient
import uk.tvidal.kraft.message.client.ClientAppendMessage
import uk.tvidal.kraft.storage.KRaftEntries
import uk.tvidal.kraft.transport.MessageSender
import java.util.UUID
import java.util.concurrent.CompletableFuture.completedFuture
import java.util.concurrent.Future

class KRaftProducerImpl internal constructor(
    server: MessageSender,
    override val mode: ClientAckType
) : AbstractKRaftClient(server), KRaftProducer {

    override fun publish(entries: KRaftEntries): Future<ProducerResponse> {
        val message = ClientAppendMessage(self, entries)
        server.send(message)
        return completedFuture(
            ProducerResponse(
                id = UUID.randomUUID(),
                mode = mode,
                node = server.node,
                error = null,
                leader = null,
                range = null,
                term = null
            )
        )
    }
}
