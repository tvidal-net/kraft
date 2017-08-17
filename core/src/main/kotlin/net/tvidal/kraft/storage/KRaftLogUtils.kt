package net.tvidal.kraft.storage

import com.google.common.primitives.Longs
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.Charset.defaultCharset

private val EMPTY_PAYLOAD = ByteArray(0)
private val EMPTY_ENTRY = KRaftEntry(0L, EMPTY_PAYLOAD)

private val EMPTY_BATCH = KRaftEntryBatch(listOf())

fun emptyEntry() = EMPTY_ENTRY

fun emptyEntry(term: Long) = KRaftEntry(term, EMPTY_PAYLOAD)

fun entryOf(term: Long, payload: ByteArray) = KRaftEntry(term, payload)

fun entryOf(term: Long, payload: ByteBuffer) = entryOf(term, payload.array())

fun entryOf(term: Long, payload: Long) = entryOf(term, Longs.toByteArray(payload))

fun entryOf(term: Long, payload: String, charset: Charset = defaultCharset()) =
  entryOf(term, payload.toByteArray(charset))

fun KRaftEntry.asBatch() = singleEntryBatch(this)

fun KRaftEntry.longValue() = Longs.fromByteArray(payload)

fun KRaftEntry.stringValue(charset: Charset = defaultCharset()) = String(payload, charset)

fun emptyBatch() = EMPTY_BATCH

fun singleEntryBatch(entry: KRaftEntry) = KRaftEntryBatch(listOf(entry))

fun flush(term: Long) = singleEntryBatch(emptyEntry(term))

val KRaftLog.nextLogIndex get() = lastLogIndex + 1L
