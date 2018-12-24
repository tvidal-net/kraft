package uk.tvidal.kraft.client.producer

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.client.ClientAppendMessage
import uk.tvidal.kraft.storage.KRaftEntries
import uk.tvidal.kraft.transport.MessageSender
import java.util.concurrent.CompletableFuture.completedFuture
import java.util.concurrent.Future

class KRaftProducerImpl(
    val server: MessageSender,
    override val mode: ProducerMode,
    override val node: RaftNode
) : KRaftProducer {

    override fun publish(entries: KRaftEntries): Future<ProducerResponse> {
        val message = ClientAppendMessage(node, entries)
        server.send(message)
        return completedFuture(
            ProducerResponse(
                id = null,
                mode = mode,
                node = server.node,
                leader = null,
                index = null,
                term = null
            )
        )
    }
}
