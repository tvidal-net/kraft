package uk.tvidal.kraft.server

interface KRaftServer {

    fun start()

    fun stop()

    fun publish(data: ByteArray) = publish(listOf(data))

    fun publish(data: List<ByteArray>)
}
