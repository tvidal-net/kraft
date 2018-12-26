package uk.tvidal.kraft.buffer

import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.util.Stack

class ByteBufferStream(buffer: ByteBuffer) {

    constructor(size: Int = 1024) : this(ByteBuffer.allocate(size))

    constructor(file: File, size: Long) : this(openMemoryMappedFile(file, size))

    companion object {
        private val emptyBuffer = ByteBuffer.allocate(0)
    }

    var buffer: ByteBuffer = buffer
        private set

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

    fun force() {
        val mappedByteBuffer = buffer as? MappedByteBuffer
        mappedByteBuffer?.force()
    }

    fun release() {
        // remove the reference to the byteBuffer when releasing
        buffer.let {
            buffer = emptyBuffer
            it.release()
        }
    }

    private val mark = Stack<Int>()

    operator fun <T> invoke(block: ByteBuffer.() -> T): T = with(buffer) {
        mark.push(position)
        try {
            block()
        } finally {
            position = mark.pop()
        }
    }

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

    override fun toString() = buffer.toString()
}
