package uk.tvidal.kraft

import uk.tvidal.kraft.storage.entries
import uk.tvidal.kraft.storage.entryOf

const val TEST_SIZE = 11

val testEntry = entryOf("12345678901")

val testEntries = entries(
    (0 until TEST_SIZE).map { testEntry }
)

fun longEntries(term: Long, range: LongRange) = range
    .map { entryOf(it, term) }
    .let { entries(it) }
