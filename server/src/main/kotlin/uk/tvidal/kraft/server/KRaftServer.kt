package uk.tvidal.kraft.server

interface KRaftServer : AutoCloseable {

    fun start()

    fun stop()

    fun publish(payload: ByteArray)
}
