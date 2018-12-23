package uk.tvidal.kraft.codec.json

import com.github.salomonbrys.kotson.registerTypeAdapter
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonToken.NULL
import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.storage.KRaftEntries
import uk.tvidal.kraft.storage.KRaftEntry

val gson = GsonBuilder()
    .registerTypeAdapter<RaftNode>(RaftNodeAdapter())
    .registerTypeAdapter<KRaftEntry>(KRaftEntryAdapter())
    .registerTypeAdapter<KRaftEntries>(KRaftEntriesAdapter())
    .setPrettyPrinting()
    .create()!!

inline fun <T> JsonReader.nullable(block: JsonReader.(JsonToken) -> T): T? {
    val token = peek()
    return if (token == NULL) null
    else block(token)
}

fun <T> JsonReader.nextArray(adapter: TypeAdapter<T>): List<T> = sequence {
    beginArray()
    while (hasNext()) {
        yield(adapter.read(this@nextArray))
    }
    endArray()
}.toList()
