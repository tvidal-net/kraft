package uk.tvidal.kraft.streaming

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.tvidal.kraft.storage.entries
import uk.tvidal.kraft.storage.entryOf
import kotlin.test.assertEquals

internal class DataWindowTest {

    private val byte = ByteArray(1)

    val window = DataWindow(10)

    @BeforeEach
    internal fun setUp() {
        window.reset()
    }

    @Test
    internal fun `consume data correctly`() {
        window.consume(1, bytes(2))
        window.consume(2, bytes(3))
        assertEquals(5, window.consumed)
        assertEquals(5, window.available)
    }

    @Test
    internal fun `releases data correctly`() {
        window.consume(1, bytes(3))
        window.consume(4, bytes(2))
        window.consume(6, bytes(3))
        assertEquals(8, window.consumed)
        assertEquals(2, window.available)

        window.release(4)
        assertEquals(5, window.consumed)
        assertEquals(5, window.available)

        window.release(5)
        assertEquals(3, window.consumed)
        assertEquals(7, window.available)
    }

    @Test
    internal fun `clear data`() {
        window.consume(1, bytes(4))
        assertEquals(6, window.available)

        window.reset()
        assertEquals(0, window.consumed)
        assertEquals(10, window.available)
    }

    private fun bytes(bytes: Int) = entries(
        (1..bytes).map {
            entryOf(byte)
        }
    )
}
