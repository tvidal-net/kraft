package uk.tvidal.kraft.engine

import uk.tvidal.kraft.config.KRaftServerConfig
import uk.tvidal.kraft.consumer.RaftConsumerState
import uk.tvidal.kraft.engine.RaftRole.ERROR
import uk.tvidal.kraft.engine.RaftRole.LEADER
import uk.tvidal.kraft.logging.KRaftLogging
import uk.tvidal.kraft.message.client.ClientAppendMessage
import uk.tvidal.kraft.message.client.ConsumerAckMessage
import uk.tvidal.kraft.message.client.ConsumerRegisterMessage
import uk.tvidal.kraft.message.raft.AppendAckMessage
import uk.tvidal.kraft.message.raft.RaftMessage
import uk.tvidal.kraft.storage.entryOf

class RaftServer internal constructor(
    config: KRaftServerConfig
) : RaftEngine(config) {

    private companion object : KRaftLogging()

    val followers = others
        .associate { it to RaftFollower(this, sender(it)) }

    private val consumers = RaftConsumerState(transport, storage, commitIndex)

    private val messages = config.transport.receiver()

    override fun publish(payload: ByteArray) {
        messages.offer(
            ClientAppendMessage(
                from = clientNode,
                data = entryOf(payload, term).toEntries()
            )
        )
    }

    internal fun clientAppend(message: ClientAppendMessage) {
        val currentLeader = leader
        if (role == LEADER) {
            val entries = message.data.copy(term)
            val lastLogIndex = storage.append(entries = entries)
            log.debug { "[$self] clientAppend lastLogIndex=$lastLogIndex msg=$message" }
            if (isSingleNodeCluster) {
                log.debug { "[$self] commit log=$storage from=$commitIndex leaderCommitIndex=$lastLogIndex" }
                commit(lastLogIndex)
            }
        } else if (currentLeader != null) {
            message.relay = self
            sender(currentLeader)
                .send(message)
        }
    }

    override fun processAck(msg: AppendAckMessage) {
        val state = followers[msg.from]
        if (msg.ack) state?.ack(msg.matchIndex)
        else state?.nack(msg.matchIndex)
    }

    override fun updateTerm(newTerm: Long) {
        log.trace { "[$self] updateTerm T$term newTerm=$newTerm" }
        term = newTerm
        messages.removeIf { it is RaftMessage && it.term < newTerm }
    }

    override fun run(now: Long) {
        try {
            val msg = messages.poll()
            when (msg) {
                is RaftMessage -> processMessage(now, msg)
                is ClientAppendMessage -> clientAppend(msg)
                is ConsumerRegisterMessage -> consumers.register(msg)
                is ConsumerAckMessage -> consumers.ack(msg)
            }
            val newRole = role.run(now, this)
            updateRole(now, newRole)
        } catch (e: Error) {
            updateRole(now, ERROR)
            throw e
        }
    }

    private fun processMessage(now: Long, msg: RaftMessage) {
        if (msg.from in cluster) {
            do {
                val newRole = role.process(now, msg, this)
                updateRole(now, newRole)
            } while (newRole != null)
        } else log.warn { "[$self] received raft message from node outside cluster ${msg.from}" }
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
                log.debug { "[$self] commit log=$storage from=$commitIndex quorumCommitIndex=$quorumCommitIndex" }
                commit(quorumCommitIndex)
            } else {
                log.warn { "SKIPPING quorumCommitIndex=$quorumCommitIndex quorumCommitTerm=$quorumCommitTerm currentTerm=$term" }
            }
        }
    }

    private fun commit(index: Long) {
        leaderCommitIndex = index
        commitIndex = index
        storage.commit(index)
        followers.values.forEach(RaftFollower::commit)
        consumers.commit(index)
    }
}
