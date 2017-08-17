package net.tvidal.kraft.processing

import net.tvidal.kraft.NEVER
import net.tvidal.kraft.message.raft.AppendAckMessage
import net.tvidal.kraft.transport.MessageSender

class RaftFollowerState(

  private val raft: RaftEngine,
  private val sender: MessageSender

) {

    val follower = sender.node

    var nextHeartBeat = NEVER; private set
    var streaming = false; private set

    var nextIndex = 0L; private set

    var matchIndex = 0L; private set

    fun reset() {

    }

    fun work(now: Long) {
        if (streaming) {
            return
        }
        heartbeat(now)
    }

    fun heartbeat(now: Long) {

    }

    fun commit() {

    }

    fun handleAck(msg: AppendAckMessage) {

    }

}
