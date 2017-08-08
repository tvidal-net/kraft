package net.tvidal.kraft.storage

import net.tvidal.kraft.domain.RaftEntry

interface RaftStorage<out T : RaftEntry> {

    fun append(entries: RaftEntry)

    fun read(): List<T>

}
