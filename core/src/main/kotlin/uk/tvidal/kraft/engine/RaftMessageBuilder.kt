package uk.tvidal.kraft.engine

import uk.tvidal.kraft.domain.RaftNode
import uk.tvidal.kraft.message.raft.AppendAckMessage
import uk.tvidal.kraft.message.raft.AppendMessage
import uk.tvidal.kraft.message.raft.RequestVoteMessage
import uk.tvidal.kraft.message.raft.VoteMessage
import uk.tvidal.kraft.storage.KRaftEntries

internal fun RaftEngine.sendVote(to: RaftNode, vote: Boolean) {
    val msg = VoteMessage(self, term, vote)
    transport.sender(to).send(msg)
}

internal fun RaftEngine.sendAck(to: RaftNode, matchIndex: Long) {
    val msg = AppendAckMessage(self, term, true, matchIndex)
    transport.sender(to).send(msg)
}

internal fun RaftEngine.sendNack(to: RaftNode, nackIndex: Long) {
    val msg = AppendAckMessage(self, term, false, nackIndex)
    transport.sender(to).send(msg)
}

internal fun RaftEngine.sendRequestVotes() {
    for (to in others) {
        val msg = RequestVoteMessage(self, term, lastLogTerm, lastLogIndex)
        transport.sender(to).send(msg)
    }
}

internal fun RaftEngine.heartbeat(prevIndex: Long, prevTerm: Long, data: KRaftEntries) =
    AppendMessage(self, term, prevTerm, prevIndex, commitIndex, data)
