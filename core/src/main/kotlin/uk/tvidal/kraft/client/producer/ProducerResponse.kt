package uk.tvidal.kraft.client.producer

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.client.ClientErrorType
import java.util.UUID

data class ProducerResponse(
    val id: UUID,
    val mode: ClientAckType,
    val node: RaftNode,
    val error: ClientErrorType?,
    val leader: RaftNode?,
    val range: LongRange?,
    val term: Long?
)
