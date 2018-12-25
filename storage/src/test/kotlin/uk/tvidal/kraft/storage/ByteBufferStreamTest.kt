package uk.tvidal.kraft.storage

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.tvidal.kraft.codec.binary.BinaryCodec.Entry
import uk.tvidal.kraft.codec.binary.toProto
import uk.tvidal.kraft.codec.binary.uuid
import java.nio.ByteBuffer
import java.util.UUID
import kotlin.test.assertEquals

internal class ByteBufferOutputStreamTest {

    val id = UUID.randomUUID()!!

    val buffer: ByteBuffer = ByteBuffer.allocate(128)

    val outputStream = ByteBufferOutputStream(buffer)

    val inputStream = ByteBufferInputStream(buffer)

    @BeforeEach
    internal fun setUp() {
        buffer.clear()
    }

    @Test
    internal fun `writes to the underlying byteBuffer`() {
        val toWrite = id.toProto()
        toWrite.writeDelimitedTo(outputStream)
        buffer.flip()

        val fromRead = Entry.parseDelimitedFrom(inputStream)
        assertEquals(id, uuid(fromRead))
    }
}
