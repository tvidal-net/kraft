package uk.tvidal.kraft.storage.buffer

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.tvidal.kraft.codec.binary.BinaryCodec.UniqueID
import uk.tvidal.kraft.codec.binary.toProto
import uk.tvidal.kraft.codec.binary.uuid
import java.nio.ByteBuffer
import java.util.UUID
import kotlin.test.assertEquals

internal class ByteBufferStreamTest {

    val id = UUID.randomUUID()!!

    val buffer: ByteBuffer = ByteBuffer.allocate(128)

    val stream = ByteBufferStream(buffer)

    @BeforeEach
    internal fun setUp() {
        buffer.clear()
    }

    @Test
    internal fun `can read and write to the underlying buffer`() {

        id.toProto()
            .writeDelimitedTo(stream.output)

        buffer.flip()

        val read = UniqueID.parseDelimitedFrom(stream.input)
        assertEquals(id, uuid(read))
    }
}
