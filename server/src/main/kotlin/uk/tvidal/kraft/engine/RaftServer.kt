package uk.tvidal.kraft.engine

import uk.tvidal.kraft.config.KRaftConfig
import uk.tvidal.kraft.engine.RaftRole.ERROR
import uk.tvidal.kraft.engine.RaftRole.LEADER
import uk.tvidal.kraft.message.client.ClientAppendMessage
import uk.tvidal.kraft.message.raft.AppendAckMessage
import uk.tvidal.kraft.message.raft.RaftMessage
import uk.tvidal.kraft.storage.KRaftEntries

class RaftServer internal constructor(
    config: KRaftConfig
) : RaftEngine(config) {

    val followers = others
        .associate { it to RaftFollower(this, sender(it)) }

    private val messages = config.transport.receiver()

    override fun publish(entries: KRaftEntries) {
        messages.offer(ClientAppendMessage(clientNode, entries))
    }

    internal fun clientAppend(msg: ClientAppendMessage) {
        val currentLeader = leader
        if (role == LEADER) {
            val entries = msg.data.copy(term)
            val lastLogIndex = storage.append(entries)
            log.debug { "$self clientAppend (${msg.from}) ${msg.data} $storage" }
            if (isSingleNodeCluster) {
                leaderCommitIndex = lastLogIndex
                commitIndex = lastLogIndex
            }
        } else if (currentLeader != null) {
            log.debug { "$self clientAppend (${msg.from}) forward to $leader ${msg.data}" }
            sender(currentLeader).send(msg)
        }
    }

    override fun processAck(msg: AppendAckMessage) {
        val state = followers[msg.from]
        if (msg.ack) state?.ack(msg.matchIndex)
        else state?.nack(msg.matchIndex)
    }

    override fun updateTerm(newTerm: Long) {
        log.info { "$self updateTerm T$term newTerm=$newTerm" }
        term = newTerm
        messages.removeIf { it is RaftMessage && it.term < newTerm }
    }

    override fun run(now: Long) {
        try {
            val msg = messages.poll()
            when (msg) {
                is RaftMessage -> processMessage(now, msg)
                is ClientAppendMessage -> clientAppend(msg)
            }
            val newRole = role.run(now, this)
            updateRole(now, newRole)
        } catch (e: Error) {
            updateRole(now, ERROR)
            throw e
        }
    }

    private fun processMessage(now: Long, msg: RaftMessage) {
        do {
            val newRole = role.process(now, msg, this)
            updateRole(now, newRole)
        } while (newRole != null)
    }

    private fun updateRole(now: Long, newRole: RaftRole?) {
        if (newRole != null) {
            role.exit(now, this)
            role = newRole
            role.enter(now, this)
        }
    }

    override fun heartbeatFollowers(now: Long) {
        followers.values.forEach { it.run(now) }
    }

    override fun resetFollowers() {
        followers.values.forEach(RaftFollower::reset)
    }

    override fun computeCommitIndex() {
        val followerIndices = followers.values.map(RaftFollower::matchIndex)
        val matchIndices = sequenceOf(lastLogIndex, *followerIndices.toTypedArray())
        val quorumCommitIndex = matchIndices
            .sorted()
            .take(cluster.majority)
            .last()

        if (quorumCommitIndex > commitIndex) {
            val quorumCommitTerm = storage.termAt(quorumCommitIndex)
            if (quorumCommitTerm == term) {
                log.info { "$self updateCommitIndex=$quorumCommitIndex from=$commitIndex" }
                leaderCommitIndex = quorumCommitIndex
                commitIndex = quorumCommitIndex
                followers.values.forEach(RaftFollower::commit)
            } else {
                log.warn { "SKIPPING quorumCommitIndex=$quorumCommitIndex quorumCommitTerm=$quorumCommitTerm currentTerm=$term" }
            }
        }
    }
}
