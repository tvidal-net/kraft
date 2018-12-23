package uk.tvidal.kraft

import uk.tvidal.kraft.storage.entries
import uk.tvidal.kraft.storage.entryOf

fun longEntries(term: Long, range: LongRange) = range
    .map { entryOf(it, term) }
    .let { entries(it) }
