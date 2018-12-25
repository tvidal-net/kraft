package uk.tvidal.kraft.storage

import java.nio.ByteBuffer

private fun unmap(buffer: ByteBuffer) {
    // this is very dangerous and should be used sparingly and with cautious
    val directBuffer = buffer as? sun.nio.ch.DirectBuffer
    val cleaner = directBuffer?.cleaner()
    cleaner?.clean()
}

internal fun ByteBuffer.release() = unmap(this)
