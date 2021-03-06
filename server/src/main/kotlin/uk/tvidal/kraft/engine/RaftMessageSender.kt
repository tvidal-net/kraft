package uk.tvidal.kraft.engine

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.client.ClientAppendAckMessage
import uk.tvidal.kraft.message.client.ClientAppendMessage
import uk.tvidal.kraft.message.client.ClientErrorType
import uk.tvidal.kraft.message.raft.AppendAckMessage
import uk.tvidal.kraft.message.raft.AppendMessage
import uk.tvidal.kraft.message.raft.RequestVoteMessage
import uk.tvidal.kraft.message.raft.VoteMessage
import uk.tvidal.kraft.storage.KRaftEntries
import uk.tvidal.kraft.transport.KRaftTransport

internal interface RaftMessageSender : RaftState {

    val transport: KRaftTransport

    fun sender(to: RaftNode) = transport.sender(to)

    fun append(to: RaftNode, prevIndex: Long, prevTerm: Long, data: KRaftEntries) = sender(to)
        .send(AppendMessage(self, term, prevTerm, prevIndex, leaderCommitIndex, data))

    fun ack(to: RaftNode, matchIndex: Long) = sender(to)
        .respond(AppendAckMessage(self, term, true, matchIndex))

    fun nack(to: RaftNode, nackIndex: Long) = sender(to)
        .respond(AppendAckMessage(self, term, false, nackIndex))

    fun requestVotes() = others
        .forEach { requestVote(it) }

    fun requestVote(to: RaftNode) = sender(to)
        .send(RequestVoteMessage(self, term, lastLogTerm, lastLogIndex))

    fun vote(to: RaftNode, vote: Boolean) = sender(to)
        .respond(VoteMessage(self, term, vote))

    fun ClientAppendMessage.relay(leader: RaftNode) = sender(leader)
        .send(ClientAppendMessage(self, data, from, ackType))

    fun ClientAppendMessage.ack(range: LongRange) = sender(from)
        .send(ClientAppendAckMessage(self, id!!, null, leader, range, term, relay))

    fun ClientAppendMessage.nack(error: ClientErrorType) = sender(from)
        .send(ClientAppendAckMessage(self, id!!, error, leader, null, term, relay))

    fun ClientAppendAckMessage.relay() = sender(relay!!)
        .send(ClientAppendAckMessage(self, id, error, leader, range, term, null))
}
