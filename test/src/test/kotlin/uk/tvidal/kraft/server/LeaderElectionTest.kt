package uk.tvidal.kraft.server

import org.junit.jupiter.api.Test
import uk.tvidal.kraft.KRaftClusterTest
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class LeaderElectionTest : KRaftClusterTest() {

    @Test
    internal fun `test leader election`() {
        assertEquals(cluster.nodes[0], actual = cluster.leader)
    }

    @Test
    internal fun `trigger an election`() {
        triggerElection(follower)
        assertNull(cluster.leader)
    }
}
