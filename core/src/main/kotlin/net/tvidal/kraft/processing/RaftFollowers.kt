package net.tvidal.kraft.processing

import net.tvidal.kraft.message.raft.AppendAckMessage

interface RaftFollowers {

    fun reset()

    fun work(now: Long)

    fun handleAck(msg: AppendAckMessage)

    fun updateCommitIndex()

}
