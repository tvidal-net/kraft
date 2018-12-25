package uk.tvidal.kraft.storage

import uk.tvidal.kraft.LONG_BYTES
import java.nio.ByteBuffer
import java.nio.ByteBuffer.allocate
import java.nio.ByteBuffer.wrap
import java.nio.charset.Charset
import java.nio.charset.Charset.defaultCharset

private val EMPTY_PAYLOAD = ByteArray(0)
private val EMPTY_ENTRY = KRaftEntry(EMPTY_PAYLOAD, 0L)

private val EMPTY_ENTRIES = KRaftEntries(emptyList())

fun emptyEntry() = EMPTY_ENTRY

fun emptyEntry(term: Long) = KRaftEntry(EMPTY_PAYLOAD, term)

fun entryOf(payload: ByteArray, term: Long = 0L) = KRaftEntry(payload, term)

fun entryOf(payload: ByteBuffer, term: Long = 0L) = KRaftEntry(payload.array(), term)

fun entryOf(payload: Long, term: Long = 0L) = KRaftEntry(payload.toByteArray(), term)

fun entryOf(payload: String, term: Long = 0L, charset: Charset = defaultCharset()) =
    KRaftEntry(payload.toByteArray(charset), term)

fun KRaftEntry.longValue() = with(wrap(payload)) {
    getLong(0)
}

fun KRaftEntry.stringValue(charset: Charset = defaultCharset()) = String(payload, charset)

fun emptyEntries() = EMPTY_ENTRIES

fun singleEntry(entry: KRaftEntry) = KRaftEntries(listOf(entry))

fun entries(entries: Collection<KRaftEntry>) = KRaftEntries(entries)

fun entries(entries: Iterable<KRaftEntry>) = KRaftEntries(entries.toList())

fun entries(vararg entries: KRaftEntry) = entries(entries.asIterable())

fun flush(term: Long) = singleEntry(emptyEntry(term))

private fun Long.toByteArray() = with(allocate(LONG_BYTES)) {
    putLong(0, this@toByteArray)
    array()
}
