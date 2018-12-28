package uk.tvidal.kraft.codec.binary

import org.junit.jupiter.api.Test
import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.client.clientNode
import uk.tvidal.kraft.codec.binary.BinaryCodec.MessageProto
import uk.tvidal.kraft.codec.binary.ProtoMessageCodec.decode
import uk.tvidal.kraft.codec.binary.ProtoMessageCodec.encode
import uk.tvidal.kraft.message.Message
import uk.tvidal.kraft.message.client.ClientAppendMessage
import uk.tvidal.kraft.message.raft.AppendAckMessage
import uk.tvidal.kraft.message.raft.AppendMessage
import uk.tvidal.kraft.message.raft.RaftMessageType.APPEND
import uk.tvidal.kraft.message.raft.RequestVoteMessage
import uk.tvidal.kraft.message.raft.VoteMessage
import uk.tvidal.kraft.message.transport.ConnectMessage
import uk.tvidal.kraft.message.transport.HeartBeatMessage
import uk.tvidal.kraft.storage.entries
import uk.tvidal.kraft.storage.entryOf
import java.lang.System.currentTimeMillis
import java.util.UUID
import kotlin.test.assertEquals

internal class ProtoMessageCodecTest {

    private val from = RaftNode(1801)
    private val term = 1982L
    private val data = entries(entryOf("Test1", 1L), entryOf("Test2", 2L))

    @Test
    internal fun `test AppendMessage`() {
        val prevTerm = 0xEEL
        val prevIndex = 0xDDL
        val leaderCommitIndex = 0xCCL
        AppendMessage(from, term, prevTerm, prevIndex, leaderCommitIndex, data)
            .assertEncodeDecode {
                assertEquals(APPEND.name, actual = messageType)
                assertEquals(2, actual = dataCount)
                assertEquals(data, actual = entries(dataList.map(::entryOf)))
            }
    }

    @Test
    internal fun `test AppendAckMessage`() {
        val ack = true
        val matchIndex = 0xFEEL
        AppendAckMessage(from, term, ack, matchIndex)
            .assertEncodeDecode()
    }

    @Test
    internal fun `test RequestVote`() {
        val lastLogTerm = 0xDDL
        val lastLogIndex = 0xFEEL
        RequestVoteMessage(from, term, lastLogTerm, lastLogIndex)
            .assertEncodeDecode()
    }

    @Test
    internal fun `test VoteMessage`() {
        val vote = false
        VoteMessage(from, term, vote)
            .assertEncodeDecode()
    }

    @Test
    internal fun `test ClientAppendMessage`() {
        val relay = clientNode("Producer")
        val id = UUID.randomUUID()
        ClientAppendMessage(from, data, relay, id)
            .assertEncodeDecode()
    }

    @Test
    internal fun `test ConnectMessage`() {
        ConnectMessage(from)
            .assertEncodeDecode()
    }

    @Test
    internal fun `test HeartBeatMessage`() {
        val time = currentTimeMillis()
        val ping = false
        HeartBeatMessage(from, time, ping)
            .assertEncodeDecode()
    }

    private fun Message.assertEncodeDecode(block: MessageProto.() -> Unit = {}) {
        val encoded = encode(this)
        System.err.println(encoded)
        block(encoded)

        val decoded = decode(encoded)
        assertEquals(this, actual = decoded)
    }
}
