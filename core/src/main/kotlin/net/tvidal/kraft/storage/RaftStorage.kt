package net.tvidal.kraft.storage

interface RaftStorage<out T : RaftEntry> {

    fun append(entries: RaftEntry)

    fun read(): List<T>

}
