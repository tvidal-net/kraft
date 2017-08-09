package net.tvidal.kraft.message.raft

import net.tvidal.kraft.domain.RaftNode
import net.tvidal.kraft.processing.RaftEngine

fun RaftEngine.vote(to: RaftNode, vote: Boolean) {
    val msg = VoteMessage(self, term, vote)
    transport.sender(to).send(msg)
}

fun RaftEngine.ack(to: RaftNode, matchIndex: Long) {
    val msg = AppendAckMessage(self, term, true, matchIndex)
    transport.sender(to).send(msg)
}

fun RaftEngine.nack(to: RaftNode, nackIndex: Long) {
    val msg = AppendAckMessage(self, term, false, nackIndex)
    transport.sender(to).send(msg)
}

fun RaftEngine.requestVotes() {
    for (to in others) {
        val msg = RequestVoteMessage(self, term, lastLogTerm, lastLogIndex)
        transport.sender(to).send(msg)
    }
}
