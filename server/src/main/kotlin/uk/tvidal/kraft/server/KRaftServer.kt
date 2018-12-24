package uk.tvidal.kraft.server

interface KRaftServer {

    fun start()

    fun stop()

    fun publish(payload: ByteArray)
}
