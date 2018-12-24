package uk.tvidal.kraft.codec.binary

import uk.tvidal.kraft.codec.binary.KRaftEntry.Entry
import java.util.UUID

fun UUID.toProto(): Entry = Entry.newBuilder()
    .setHigh(mostSignificantBits)
    .setLow(leastSignificantBits)
    .build()

fun uuid(id: Entry) = UUID(id.high, id.low)
