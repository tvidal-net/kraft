package net.tvidal.kraft.processing

import net.tvidal.kraft.domain.RaftNode
import net.tvidal.kraft.message.raft.AppendAckMessage

class RaftFollowerView(

  val follower: RaftNode

) {

    var matchIndex = 0L; private set

    fun reset() {

    }

    fun work(now: Long) {

    }

    fun heartbeat(now: Long) {

    }

    fun commit() {

    }

    fun handleAck(msg: AppendAckMessage) {

    }

}
