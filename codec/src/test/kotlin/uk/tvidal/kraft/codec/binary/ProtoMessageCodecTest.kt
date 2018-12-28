package uk.tvidal.kraft.codec.binary

import org.junit.jupiter.api.Test
import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.codec.binary.ProtoMessageCodec.decode
import uk.tvidal.kraft.codec.binary.ProtoMessageCodec.encode
import uk.tvidal.kraft.message.raft.AppendMessage
import uk.tvidal.kraft.message.raft.RaftMessageType.APPEND
import uk.tvidal.kraft.storage.entries
import uk.tvidal.kraft.storage.entryOf
import kotlin.test.assertEquals

internal class ProtoMessageCodecTest {

    @Test
    internal fun `test AppendMessage`() {
        val from = RaftNode(1801)
        val term = 1982L
        val prevTerm = 0xEEL
        val prevIndex = 0xDDL
        val leaderCommitIndex = 0xCCL
        val data = entries(entryOf("Test1", 1L), entryOf("Test2", 2L))
        val message = AppendMessage(from, term, prevTerm, prevIndex, leaderCommitIndex, data)

        val encoded = encode(message)
        assertEquals(from, actual = encoded.from.toNode())
        assertEquals(APPEND.name, actual = encoded.messageType)
        assertEquals(2, actual = encoded.dataCount)
        assertEquals(data, actual = entries(encoded.dataList.map(::entryOf)))
        System.err.println(encoded)

        val decoded = decode(encoded)
        assertEquals(message, actual = decoded)
    }
}
