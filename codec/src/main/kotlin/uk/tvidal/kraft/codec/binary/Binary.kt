package uk.tvidal.kraft.codec.binary

import com.google.protobuf.ByteString
import uk.tvidal.kraft.codec.binary.BinaryCodec.DataEntry
import uk.tvidal.kraft.codec.binary.BinaryCodec.Entry
import uk.tvidal.kraft.codec.binary.BinaryCodec.EntryType.DEFAULT
import uk.tvidal.kraft.storage.KRaftEntry
import java.util.UUID

fun UUID.toProto(): Entry = Entry.newBuilder()
    .setHigh(mostSignificantBits)
    .setLow(leastSignificantBits)
    .build()

fun uuid(id: Entry) = UUID(id.high, id.low)

fun KRaftEntry.toProto(): DataEntry = DataEntry.newBuilder()
    .setId(id.toProto())
    .setType(DEFAULT)
    .setPayload(ByteString.copyFrom(payload))
    .setTerm(term)
    .build()
