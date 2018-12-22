package uk.tvidal.kraft

import uk.tvidal.kraft.storage.KRaftEntries
import uk.tvidal.kraft.storage.entryOf

fun longEntries(term: Long, range: LongRange) = range
    .map { entryOf(term, it) }
    .let { KRaftEntries(it) }
