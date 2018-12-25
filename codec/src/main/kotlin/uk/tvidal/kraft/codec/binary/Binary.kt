package uk.tvidal.kraft.codec.binary

import com.google.protobuf.ByteString
import com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag
import com.google.protobuf.MessageLite
import uk.tvidal.kraft.codec.binary.BinaryCodec.DataEntry
import uk.tvidal.kraft.codec.binary.BinaryCodec.EntryType.DEFAULT
import uk.tvidal.kraft.codec.binary.BinaryCodec.UniqueID
import uk.tvidal.kraft.storage.KRaftEntry
import java.util.UUID

fun UUID.toProto(): UniqueID = UniqueID.newBuilder()
    .setHigh(mostSignificantBits)
    .setLow(leastSignificantBits)
    .build()

fun uuid(id: UniqueID) = UUID(id.high, id.low)

fun KRaftEntry.toProto(): DataEntry = DataEntry.newBuilder()
    .setId(id.toProto())
    .setType(DEFAULT)
    .setPayload(ByteString.copyFrom(payload))
    .setTerm(term)
    .build()

fun entryOf(proto: DataEntry) = KRaftEntry(
    id = uuid(proto.id),
    term = proto.term,
    payload = proto.payload.toByteArray()
)

fun computeSerialisedSize(entry: MessageLite): Int {
    val messageBytes = entry.serializedSize
    val sizeBytes = computeUInt32SizeNoTag(messageBytes)
    return messageBytes + sizeBytes
}

