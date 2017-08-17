package net.tvidal.kraft.processing

import net.tvidal.kraft.domain.RaftNode
import net.tvidal.kraft.message.raft.AppendAckMessage
import net.tvidal.kraft.message.raft.AppendMessage
import net.tvidal.kraft.message.raft.RequestVoteMessage
import net.tvidal.kraft.message.raft.VoteMessage
import net.tvidal.kraft.storage.KRaftEntries

internal fun RaftEngine.vote(to: RaftNode, vote: Boolean) {
    val msg = VoteMessage(self, term, vote)
    transport.sender(to).send(msg)
}

internal fun RaftEngine.ack(to: RaftNode, matchIndex: Long) {
    val msg = AppendAckMessage(self, term, true, matchIndex)
    transport.sender(to).send(msg)
}

internal fun RaftEngine.nack(to: RaftNode, nackIndex: Long) {
    val msg = AppendAckMessage(self, term, false, nackIndex)
    transport.sender(to).send(msg)
}

internal fun RaftEngine.requestVotes() {
    for (to in others) {
        val msg = RequestVoteMessage(self, term, lastLogTerm, lastLogIndex)
        transport.sender(to).send(msg)
    }
}

internal fun RaftEngine.heartbeat(prevIndex: Long, prevTerm: Long, data: KRaftEntries) =
  AppendMessage(self, term, prevTerm, prevIndex, commitIndex, data)
