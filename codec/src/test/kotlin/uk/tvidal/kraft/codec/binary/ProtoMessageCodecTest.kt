package uk.tvidal.kraft.codec.binary

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.JsonObject
import org.junit.jupiter.api.Test
import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.client.clientNode
import uk.tvidal.kraft.codec.binary.ProtoMessageCodec.decode
import uk.tvidal.kraft.codec.binary.ProtoMessageCodec.encode
import uk.tvidal.kraft.codec.json.gson
import uk.tvidal.kraft.message.Message
import uk.tvidal.kraft.message.client.ClientAppendAckMessage
import uk.tvidal.kraft.message.client.ClientAppendMessage
import uk.tvidal.kraft.message.client.ClientErrorType.LEADER_NOT_FOUND
import uk.tvidal.kraft.message.raft.AppendAckMessage
import uk.tvidal.kraft.message.raft.AppendMessage
import uk.tvidal.kraft.message.raft.RequestVoteMessage
import uk.tvidal.kraft.message.raft.VoteMessage
import uk.tvidal.kraft.message.transport.ConnectMessage
import uk.tvidal.kraft.message.transport.HeartBeatMessage
import uk.tvidal.kraft.storage.entries
import uk.tvidal.kraft.storage.entryOf
import java.lang.System.currentTimeMillis
import kotlin.reflect.KCallable
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
            .assertEncodeDecode(
                AppendMessage::term,
                AppendMessage::prevTerm,
                AppendMessage::prevIndex,
                AppendMessage::leaderCommitIndex
            )
    }

    @Test
    internal fun `test AppendAckMessage`() {
        val ack = true
        val matchIndex = 0xFEEL
        AppendAckMessage(from, term, ack, matchIndex)
            .assertEncodeDecode(
                AppendAckMessage::ack,
                AppendAckMessage::term,
                AppendAckMessage::matchIndex
            )
    }

    @Test
    internal fun `test RequestVote`() {
        val lastLogTerm = 0xDDL
        val lastLogIndex = 0xFEEL
        RequestVoteMessage(from, term, lastLogTerm, lastLogIndex)
            .assertEncodeDecode(
                RequestVoteMessage::term,
                RequestVoteMessage::lastLogTerm,
                RequestVoteMessage::lastLogIndex
            )
    }

    @Test
    internal fun `test VoteMessage`() {
        val vote = false
        VoteMessage(from, term, vote)
            .assertEncodeDecode(
                VoteMessage::term,
                VoteMessage::vote
            )
    }

    @Test
    internal fun `test ClientAppendMessage`() {
        val relay = clientNode("Producer")
        ClientAppendMessage(from, data, relay)
            .assertEncodeDecode(
                ClientAppendMessage::relay,
                ClientAppendMessage::ackType
            )
    }

    @Test
    internal fun `test ClientAppendAckMessage`() {
        val relay = clientNode("Producer")
        val leader = from
        val range = 0xAAL..0xEEL
        ClientAppendAckMessage(from, data.id!!, LEADER_NOT_FOUND, leader, range, term, relay)
            .assertEncodeDecode(
                ClientAppendAckMessage::id,
                ClientAppendAckMessage::error,
                ClientAppendAckMessage::leader,
                ClientAppendAckMessage::range,
                ClientAppendAckMessage::term,
                ClientAppendAckMessage::relay
            )
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
            .assertEncodeDecode(
                HeartBeatMessage::time,
                HeartBeatMessage::ping
            )
    }

    private fun Message.assertEncodeDecode(vararg properties: MessageProperty<*>) {
        val encoded = encode(this)
        System.err.println(encoded)

        val decoded = decode(encoded)
        assertEquals(this, actual = decoded)

        val expected = properties
            .map(KCallable<*>::name)
            .toSet()

        val actual = gson
            .fromJson<JsonObject>(encoded.message)
            .keySet()

        assertEquals(expected, actual = actual, message = "MessageProto::message.keySet")
    }
}
