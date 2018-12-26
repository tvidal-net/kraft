package uk.tvidal.kraft.buffer

import org.junit.jupiter.api.Test
import uk.tvidal.kraft.MAGIC_NUMBER
import java.nio.ByteBuffer
import java.nio.ByteBuffer.allocate
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class ByteBufferStreamTest {

    val id = MAGIC_NUMBER

    val buffer: ByteBuffer = allocate(128)

    val stream = ByteBufferStream(buffer)

    @Test
    internal fun `can read and write to the underlying buffer`() {

        val writeArray = id.toString().toByteArray()
        stream.output.write(writeArray)

        buffer.flip()

        val readArray = ByteArray(writeArray.size)
        val length = stream.input.read(readArray)
        assertEquals(writeArray.size, actual = length)

        val read = String(readArray)
        assertEquals(id, UUID.fromString(read))
    }

    @Test
    internal fun `ensure the buffer is replaced after release`() {
        stream.release()
        assertTrue { stream.isEmpty }
        assertTrue { stream.isFull }
        assertNotEquals(buffer, actual = stream.buffer)
    }

    @Test
    internal fun `can keep track of nested position changes`() {
        stream.position = 8
        stream {
            stream.position = 5
            stream {
                position(3)
                assertEquals(3, actual = stream.position)
            }
            assertEquals(5, actual = stream.position)
        }
        assertEquals(8, actual = stream.position)
    }
}
