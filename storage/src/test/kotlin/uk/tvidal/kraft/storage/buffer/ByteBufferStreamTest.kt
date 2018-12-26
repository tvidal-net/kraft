package uk.tvidal.kraft.storage.buffer

import org.junit.jupiter.api.Test
import uk.tvidal.kraft.codec.binary.BinaryCodec.UniqueID
import uk.tvidal.kraft.storage.KRAFT_MAGIC_NUMBER
import java.nio.ByteBuffer
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class ByteBufferStreamTest {

    val id = KRAFT_MAGIC_NUMBER

    val buffer: ByteBuffer = ByteBuffer.allocate(128)

    val stream = ByteBufferStream(buffer)

    @Test
    internal fun `can read and write to the underlying buffer`() {

        id.writeDelimitedTo(stream.output)

        buffer.flip()

        val read = UniqueID.parseDelimitedFrom(stream.input)
        assertEquals(id, read)
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
