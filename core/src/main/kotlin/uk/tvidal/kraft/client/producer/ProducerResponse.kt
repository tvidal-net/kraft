package uk.tvidal.kraft.client.producer

import uk.tvidal.kraft.RaftNode
import java.util.UUID

data class ProducerResponse(
    val id: UUID?,
    val mode: ProducerMode,
    val node: RaftNode,
    val leader: RaftNode?,
    val index: Long?,
    val term: Long?
)
