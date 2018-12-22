package net.tvidal.kraft.storage

import net.tvidal.kraft.LONG_BYTES
import java.nio.ByteBuffer
import java.nio.ByteBuffer.allocate
import java.nio.ByteBuffer.wrap
import java.nio.charset.Charset
import java.nio.charset.Charset.defaultCharset

private val EMPTY_PAYLOAD = ByteArray(0)
private val EMPTY_ENTRY = KRaftEntry(0L, EMPTY_PAYLOAD)

private val EMPTY_ENTRIES = KRaftEntries(emptyList())

fun emptyEntry() = EMPTY_ENTRY

fun emptyEntry(term: Long) = KRaftEntry(term, EMPTY_PAYLOAD)

fun entryOf(term: Long, payload: ByteArray) = KRaftEntry(term, payload)

fun entryOf(term: Long, payload: ByteBuffer) = KRaftEntry(term, payload.array())

fun entryOf(term: Long, payload: Long) = KRaftEntry(term, payload.toByteArray())

fun entryOf(term: Long, payload: String, charset: Charset = defaultCharset()) =
    KRaftEntry(term, payload.toByteArray(charset))

fun KRaftEntry.longValue() = with(wrap(payload)) {
    getLong(0)
}

fun KRaftEntry.stringValue(charset: Charset = defaultCharset()) = String(payload, charset)

fun emptyEntries() = EMPTY_ENTRIES

fun singleEntry(entry: KRaftEntry) = KRaftEntries(listOf(entry))

fun entries(entries: Collection<KRaftEntry>) = KRaftEntries(entries)

fun entries(vararg entries: KRaftEntry) = entries(entries.toList())

fun flush(term: Long) = singleEntry(emptyEntry(term))

private fun Long.toByteArray() = with(allocate(LONG_BYTES)) {
    putLong(0, this@toByteArray)
    array()
}
