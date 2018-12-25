package uk.tvidal.kraft.storage

import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer

class ByteBufferStream(val buffer: ByteBuffer) {

    constructor(file: File, size: Long) : this(openMemoryMappedFile(file, size))

    val input: InputStream = ByteBufferInputStream()
    val output: OutputStream = ByteBufferOutputStream()

    var position: Int
        get() = buffer.position()
        set(value) {
            buffer.position(value)
        }

    val isEmpty: Boolean
        get() = buffer.limit() == 0

    val available: Int
        get() = buffer.remaining()

    val isFull: Boolean
        get() = available == 0

    private inner class ByteBufferInputStream : InputStream() {

        override fun read(): Int = buffer.get().toInt()

        override fun read(array: ByteArray, offset: Int, length: Int): Int {
            buffer.get(array, offset, length)
            return length
        }

        override fun available(): Int = buffer.remaining()
    }

    private inner class ByteBufferOutputStream : OutputStream() {

        override fun write(byte: Int) {
            buffer.put(byte.toByte())
        }

        override fun write(array: ByteArray, offset: Int, length: Int) {
            buffer.put(array, offset, length)
        }
    }
}
