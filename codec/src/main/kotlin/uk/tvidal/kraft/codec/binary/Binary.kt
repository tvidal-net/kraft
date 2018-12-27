package uk.tvidal.kraft.codec.binary

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.protobuf.ByteString
import com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag
import com.google.protobuf.MessageLite
import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.codec.binary.BinaryCodec.DataEntry
import uk.tvidal.kraft.codec.binary.BinaryCodec.EntryType.DEFAULT
import uk.tvidal.kraft.codec.binary.BinaryCodec.MessageProto
import uk.tvidal.kraft.codec.binary.BinaryCodec.RaftNodeProto
import uk.tvidal.kraft.codec.binary.BinaryCodec.UniqueID
import uk.tvidal.kraft.message.Message
import uk.tvidal.kraft.storage.KRaftEntries
import uk.tvidal.kraft.storage.KRaftEntry
import java.util.UUID
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.isSuperclassOf

typealias MessageProperty<T> = KProperty1<out Message, T>

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

fun RaftNode.toProto(): RaftNodeProto = RaftNodeProto.newBuilder()
    .setClientNode(clientNode)
    .setIndex(index)
    .setCluster(cluster)
    .build()

fun RaftNodeProto.toNode() = RaftNode(
    index = index,
    cluster = cluster,
    clientNode = clientNode
)

fun computeSerialisedSize(entry: MessageLite): Int {
    val messageBytes = entry.serializedSize
    val sizeBytes = computeUInt32SizeNoTag(messageBytes)
    return messageBytes + sizeBytes
}

fun MessageProto.toMessage() = ProtoMessageCodec.decode(this)

val KType.rawType: KClass<*>
    get() = classifier as KClass<*>

@Suppress("UNCHECKED_CAST")
fun <T> Gson.getAdapter(property: KCallable<T>): TypeAdapter<T> =
    getAdapter(property.returnType.rawType.java) as TypeAdapter<T>

val TypeToken<*>.kotlin
    get() = (rawType as Class<*>).kotlin

fun isPayloadProperty(property: KCallable<*>): Boolean =
    KRaftEntries::class.isSuperclassOf(property.returnType.rawType)
