package uk.tvidal.kraft.buffer

import java.nio.ByteBuffer

private fun unmap(buffer: ByteBuffer) {
    // this is very dangerous and should be used sparingly and with cautious
    // it can crash the JVM if the buffer is accessed after being unmapped
    val directBuffer = buffer as? sun.nio.ch.DirectBuffer
    val cleaner = directBuffer?.cleaner()
    cleaner?.clean()
}

internal fun ByteBuffer.release() = unmap(this)
