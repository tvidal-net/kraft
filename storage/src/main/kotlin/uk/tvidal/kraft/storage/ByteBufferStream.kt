package uk.tvidal.kraft.storage

import java.io.OutputStream
import java.nio.ByteBuffer

class ByteBufferOutputStream(val buffer: ByteBuffer): OutputStream() {
    override fun write(b: Int) {
        TODO("not implemented")
    }
}
