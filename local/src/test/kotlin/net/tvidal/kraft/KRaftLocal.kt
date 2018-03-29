package net.tvidal.kraft

import net.tvidal.kraft.storage.KRaftEntries
import net.tvidal.kraft.storage.entryOf

fun longEntries(term: Long, range: LongRange) = range
  .map { entryOf(term, it) }
  .let { KRaftEntries(it) }
