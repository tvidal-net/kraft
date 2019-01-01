package uk.tvidal.kraft.client

import org.junit.jupiter.api.Test
import uk.tvidal.kraft.KRaftClusterTest
import uk.tvidal.kraft.client.producer.ClientAckType.LEADER_WRITE
import uk.tvidal.kraft.message.client.ClientAppendAckMessage
import uk.tvidal.kraft.message.client.ClientAppendMessage
import uk.tvidal.kraft.testEntries
import uk.tvidal.kraft.testEntry
import kotlin.test.assertEquals

class ProducerAckTest : KRaftClusterTest() {

    @Test
    internal fun `test producer ack on leader write`() {
        sender(leader).send(
            ClientAppendMessage(
                from = client,
                data = testEntries,
                ackType = LEADER_WRITE
            )
        )
        work()
        val message = clientMessages.poll()
        assertEquals(
            actual = message,
            expected = ClientAppendAckMessage(
                from = leader,
                id = testEntry.id,
                error = null,
                leader = leader,
                term = 1L,
                range = 2L..12,
                relay = null
            )
        )
    }

    @Test
    internal fun `reject message when there is no leader`() {
        triggerElection()
    }
}
