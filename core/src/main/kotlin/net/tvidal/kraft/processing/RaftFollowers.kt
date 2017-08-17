package net.tvidal.kraft.processing

import net.tvidal.kraft.message.raft.AppendAckMessage

internal interface RaftFollowers {

    fun reset()

    fun work(now: Long)

    fun ack(msg: AppendAckMessage)

    fun updateCommitIndex()

}
